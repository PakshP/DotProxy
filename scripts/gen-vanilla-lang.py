#!/usr/bin/env python3
"""
Generates src/main/resources/mcdata/vanilla_lang.json.gz

For every Minecraft release from --min up to the latest, this pulls straight from Mojang's client jar:
  - the raw en_us.json
  - the protocol version (version.json)
  - the default keybinds (decoded from the KeyMapping constructors in net.minecraft.client.Options)

Jars are read with HTTP range requests, so only the few entries needed are downloaded, not the full ~40MB jar.

Output is a delta-encoded history so the proxy can render text exactly as the version it is
announcing to the server would:
  {
    "format": 1,
    "versions": [{"id": "1.21.4", "protocol": 769}, ...],      # oldest -> newest
    "lang":     {key: "value" | [[fromIndex, "value" | null], ...]},
    "keybinds": {name: "W"   | [[fromIndex, "W" | null], ...]}
  }
A plain string means "present with this value since versions[0] and never changed".

Usage: python scripts/gen-vanilla-lang.py [--min 1.21.4] [--out path]
"""
import argparse
import gzip
import io
import json
import re
import struct
import sys
import urllib.request
import zipfile
from pathlib import Path

MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
UA = {"User-Agent": "vanilla-lang-generator"}


def fetch_json(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers=UA)) as r:
        return json.load(r)


class RangeFile(io.RawIOBase):
    """Seekable read-only file over HTTP range requests, with a small block cache."""
    BLOCK = 256 * 1024

    def __init__(self, url, size):
        self.url, self.size, self.pos, self.cache = url, size, 0, {}

    def readable(self): return True
    def seekable(self): return True
    def tell(self): return self.pos

    def seek(self, off, whence=0):
        self.pos = off if whence == 0 else self.pos + off if whence == 1 else self.size + off
        return self.pos

    def _block(self, i):
        if i not in self.cache:
            start = i * self.BLOCK
            end = min(start + self.BLOCK, self.size) - 1
            req = urllib.request.Request(self.url, headers={**UA, "Range": f"bytes={start}-{end}"})
            with urllib.request.urlopen(req) as r:
                if r.status != 206:
                    raise RuntimeError(f"server ignored range request for {self.url}")
                self.cache[i] = r.read()
        return self.cache[i]

    def read(self, n=-1):
        if n is None or n < 0:
            n = self.size - self.pos
        out = bytearray()
        while n > 0 and self.pos < self.size:
            b = self._block(self.pos // self.BLOCK)
            off = self.pos % self.BLOCK
            chunk = b[off:off + n]
            out += chunk
            self.pos += len(chunk)
            n -= len(chunk)
        return bytes(out)

    def readinto(self, buf):
        data = self.read(len(buf))
        buf[:len(data)] = data
        return len(data)


def find_classes(version_json, jar, deobf_names):
    """Maps deobfuscated class names (dotted) to jar entry paths."""
    entries = set(jar.namelist())
    direct = {n: n.replace(".", "/") + ".class" for n in deobf_names}
    if all(p in entries for p in direct.values()):  # unobfuscated jars (26.1+)
        return direct
    mappings = version_json["downloads"].get("client_mappings")
    if not mappings:
        raise RuntimeError("obfuscated jar without client_mappings")
    wanted = {f"{n} -> ": n for n in deobf_names}
    found = {}
    # stream the mappings and stop as soon as every class line has shown up
    with urllib.request.urlopen(urllib.request.Request(mappings["url"], headers=UA)) as r:
        for raw in r:
            line = raw.decode("utf-8").rstrip("\r\n")
            if line.startswith(" ") or " -> " not in line:
                continue
            for prefix, name in wanted.items():
                if line.startswith(prefix):
                    found[name] = line[len(prefix):].rstrip(":").replace(".", "/") + ".class"
            if len(found) == len(wanted):
                return found
    raise RuntimeError(f"classes not found in mappings: {set(deobf_names) - set(found)}")


# ---- minimal class file parsing ---------------------------------------------------------------

# operand byte length for each opcode (excluding tableswitch/lookupswitch/wide which are special)
_OPLEN = {}
for op in range(0, 256):
    _OPLEN[op] = 0
for op in (0x10, 0x12, 0x15, 0x16, 0x17, 0x18, 0x19, 0x36, 0x37, 0x38, 0x39, 0x3a, 0xa9, 0xbc):
    _OPLEN[op] = 1
for op in (0x11, 0x13, 0x14, 0x84, 0x99, 0x9a, 0x9b, 0x9c, 0x9d, 0x9e, 0x9f, 0xa0, 0xa1, 0xa2, 0xa3, 0xa4, 0xa5,
           0xa6, 0xa7, 0xa8, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xbb, 0xbd, 0xc0, 0xc1, 0xc6, 0xc7):
    _OPLEN[op] = 2
_OPLEN[0xc5] = 3
for op in (0xb9, 0xba, 0xc8, 0xc9):
    _OPLEN[op] = 4


def parse_class(data):
    """Returns (constant_pool, [code bytes of every method])."""
    p = 8
    (cp_count,) = struct.unpack_from(">H", data, p); p += 2
    cp = [None] * cp_count
    i = 1
    while i < cp_count:
        tag = data[p]; p += 1
        if tag == 1:
            (ln,) = struct.unpack_from(">H", data, p); p += 2
            cp[i] = ("utf8", data[p:p + ln].decode("utf-8", "replace")); p += ln
        elif tag == 3:
            cp[i] = ("int", struct.unpack_from(">i", data, p)[0]); p += 4
        elif tag == 4:
            cp[i] = ("float", None); p += 4
        elif tag in (5, 6):
            cp[i] = ("wide", struct.unpack_from(">q" if tag == 5 else ">d", data, p)[0]); p += 8; i += 1
        elif tag == 7:
            cp[i] = ("class", struct.unpack_from(">H", data, p)[0]); p += 2
        elif tag == 8:
            cp[i] = ("string", struct.unpack_from(">H", data, p)[0]); p += 2
        elif tag in (9, 10, 11):
            cp[i] = ("ref", struct.unpack_from(">HH", data, p)); p += 4
        elif tag == 12:
            cp[i] = ("nat", struct.unpack_from(">HH", data, p)); p += 4
        elif tag == 15:
            cp[i] = ("mh", None); p += 3
        elif tag == 16:
            cp[i] = ("mt", None); p += 2
        elif tag in (17, 18):
            cp[i] = ("indy", None); p += 4
        elif tag in (19, 20):
            cp[i] = ("mod", None); p += 2
        else:
            raise RuntimeError(f"unknown constant pool tag {tag}")
        i += 1
    p += 6  # access, this, super
    (ifaces,) = struct.unpack_from(">H", data, p); p += 2 + ifaces * 2

    def skip_members(p):
        (n,) = struct.unpack_from(">H", data, p); p += 2
        codes = []
        for _ in range(n):
            p += 6
            (ac,) = struct.unpack_from(">H", data, p); p += 2
            for _ in range(ac):
                name_idx, ln = struct.unpack_from(">HI", data, p); p += 6
                if cp[name_idx] == ("utf8", "Code"):
                    (code_len,) = struct.unpack_from(">I", data, p + 4)
                    codes.append(data[p + 8:p + 8 + code_len])
                p += ln
        return p, codes

    p, _ = skip_members(p)   # fields
    p, codes = skip_members(p)  # methods
    return cp, codes


def decode(code):
    """Yields (opcode, operand_value) for each instruction."""
    p = 0
    while p < len(code):
        op = code[p]
        start = p
        p += 1
        if op == 0xaa:  # tableswitch
            p += (4 - p % 4) % 4
            _, lo, hi = struct.unpack_from(">iii", code, p); p += 12 + (hi - lo + 1) * 4
            yield op, None; continue
        if op == 0xab:  # lookupswitch
            p += (4 - p % 4) % 4
            _, n = struct.unpack_from(">ii", code, p); p += 8 + n * 8
            yield op, None; continue
        if op == 0xc4:  # wide
            p += 5 if code[p] == 0x84 else 3
            yield op, None; continue
        n = _OPLEN[op]
        if op == 0x10:
            val = struct.unpack_from(">b", code, p)[0]
        elif op == 0x11:
            val = struct.unpack_from(">h", code, p)[0]
        elif op == 0x12:
            val = code[p]
        elif n == 2 or op in (0xb9, 0xba, 0xc5):  # cp index (invokeinterface/indy/multianewarray carry extra bytes)
            val = struct.unpack_from(">H", code, p)[0]
        else:
            val = None
        p += n
        yield op, val


def int_push(op, val, cp):
    if 0x02 <= op <= 0x08:
        return op - 0x03  # iconst_m1..iconst_5
    if op in (0x10, 0x11):
        return val
    if op in (0x12, 0x13) and cp[val][0] == "int":
        return cp[val][1]
    return None


def cp_string(cp, op, val):
    if op in (0x12, 0x13) and cp[val] and cp[val][0] == "string":
        return cp[cp[val][1]][1]
    return None


def ref_returns_int(cp, idx):
    _, (_, nat) = cp[idx]
    return cp[cp[nat][1][1]][1].endswith(")I")


def ref_field(cp, idx):
    _, (cls, nat) = cp[idx]
    return cp[cp[cls][1]][1], cp[cp[nat][1][0]][1]


def utf8s(cp):
    return {c[1] for c in cp if c and c[0] == "utf8"}


def extract_input_names(type_class_bytes):
    """
    Reads InputConstants$Type's static init: `addKey(Type.X, "key.keyboard.w", <code>)`.
    Returns (keyboard code->name, mouse code->name, the (class, field) of Type.MOUSE, backend).
    """
    cp, codes = parse_class(type_class_bytes)
    keyboard, mouse, mouse_field = {}, {}, None
    for code in codes:
        ins = list(decode(code))
        for i in range(1, len(ins) - 1):
            name = cp_string(cp, *ins[i])
            num = int_push(*ins[i + 1], cp)
            if name is None or num is None or ins[i - 1][0] != 0xb2:
                continue
            if name.startswith("key.mouse."):
                mouse.setdefault(num, name)
                mouse_field = ref_field(cp, ins[i - 1][1])
            elif name.startswith("key.keyboard."):
                keyboard.setdefault(num, name)
    strings = utf8s(cp)
    backend = "sdl" if "SDL_GetKeyName" in strings else "glfw" if "glfwGetKeyName" in strings else None
    if not keyboard or not mouse or not mouse_field or not backend:
        raise RuntimeError("could not decode InputConstants$Type")
    return keyboard, mouse, mouse_field, backend


KEY_NAME = re.compile(r"^key\.(?!categor|keyboard\.|mouse\.)[\w.\-]+$")


def extract_default_keybinds(class_bytes, mouse_field):
    """
    Finds `new KeyMapping("key.x", ...)` style constructions in Options:
      ldc "key.x"; <int>                                -> keyboard code
      ldc "key.x"; getstatic <Type.MOUSE>; <int>        -> mouse button
      ldc "key.x"; getstatic <other Type>; <int>        -> keyboard code
      ldc "key.x"; getstatic UNKNOWN; invokevirtual ()I -> unbound
    """
    cp, codes = parse_class(class_bytes)
    found = {}
    for code in codes:
        ins = list(decode(code))
        for i, (op, val) in enumerate(ins):
            s = cp_string(cp, op, val)
            if not s or not KEY_NAME.match(s) or i + 2 >= len(ins):
                continue
            op1, v1 = ins[i + 1]
            num = int_push(op1, v1, cp)
            if num is not None:
                found.setdefault(s, ("keyboard", num))
            elif op1 == 0xb2:  # getstatic
                op2, v2 = ins[i + 2]
                num = int_push(op2, v2, cp)
                if num is not None:
                    found.setdefault(s, ("mouse" if ref_field(cp, v1) == mouse_field else "keyboard", num))
                elif op2 == 0xb6 and ref_returns_int(cp, v2):
                    found.setdefault(s, ("unbound", None))
    return found


# single-character key names on a US layout. Both glfwGetKeyName and SDL_GetKeyName return
# the character for these; vanilla upper-cases it and shows it literally.
PRINTABLE = {f"key.keyboard.{c}": c for c in "abcdefghijklmnopqrstuvwxyz0123456789"}
PRINTABLE.update({
    "key.keyboard.apostrophe": "'", "key.keyboard.comma": ",", "key.keyboard.minus": "-",
    "key.keyboard.period": ".", "key.keyboard.slash": "/", "key.keyboard.semicolon": ";",
    "key.keyboard.equal": "=", "key.keyboard.left.bracket": "[", "key.keyboard.backslash": "\\",
    "key.keyboard.right.bracket": "]", "key.keyboard.grave.accent": "`",
})
# glfwGetKeyName also returns a single character for keypad keys; SDL returns "Keypad 1" etc.
GLFW_KEYPAD = {f"key.keyboard.keypad.{i}": str(i) for i in range(10)}
GLFW_KEYPAD.update({"key.keyboard.keypad.decimal": ".", "key.keyboard.keypad.divide": "/",
                    "key.keyboard.keypad.multiply": "*", "key.keyboard.keypad.subtract": "-",
                    "key.keyboard.keypad.add": "+", "key.keyboard.keypad.equal": "="})


def key_display(kind, num, inputs, lang):
    keyboard, mouse, _, backend = inputs
    unknown = lang.get("key.keyboard.unknown", "key.keyboard.unknown")
    if kind == "unbound":
        return unknown
    if kind == "mouse":
        name = mouse.get(num)
        if name and name in lang:
            return lang[name]
        n = num + 1 if backend == "glfw" else num  # glfw buttons are 0-based, sdl 1-based
        return lang.get("key.mouse", "Button %1$s").replace("%1$s", str(n)).replace("%s", str(n))
    name = keyboard.get(num)
    if name is None or name == "key.keyboard.unknown":
        return unknown
    if name in PRINTABLE:
        return PRINTABLE[name].upper()
    if backend == "glfw" and name in GLFW_KEYPAD:
        return GLFW_KEYPAD[name]
    return lang.get(name, name)


def load_version(entry):
    vjson = fetch_json(entry["url"])
    client = vjson["downloads"]["client"]
    jar = zipfile.ZipFile(io.BufferedReader(RangeFile(client["url"], client["size"]), buffer_size=64 * 1024))
    lang = json.loads(jar.read("assets/minecraft/lang/en_us.json").decode("utf-8"))
    protocol = json.loads(jar.read("version.json").decode("utf-8"))["protocol_version"]
    classes = find_classes(vjson, jar, ["net.minecraft.client.Options",
                                        "com.mojang.blaze3d.platform.InputConstants$Type"])
    inputs = extract_input_names(jar.read(classes["com.mojang.blaze3d.platform.InputConstants$Type"]))
    raw_binds = extract_default_keybinds(jar.read(classes["net.minecraft.client.Options"]), inputs[2])
    binds = {name: key_display(kind, num, inputs, lang) for name, (kind, num) in raw_binds.items()}
    return lang, protocol, inputs[3], binds


def delta_encode(snapshots):
    """snapshots: list of dicts (oldest -> newest). Returns key -> str | [[idx, val|None], ...]"""
    keys = set()
    for s in snapshots:
        keys.update(s)
    out = {}
    for k in sorted(keys):
        hist, prev = [], object()
        for i, s in enumerate(snapshots):
            v = s.get(k)
            if v != prev:
                if not (i == 0 and v is None):
                    hist.append([i, v])
                prev = v
        out[k] = hist[0][1] if len(hist) == 1 and hist[0][0] == 0 else hist
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--min", default="1.21.4")
    ap.add_argument("--out", default=str(Path(__file__).resolve().parent.parent
                                         / "src/main/resources/mcdata/vanilla_lang.json.gz"))
    args = ap.parse_args()

    releases = [v for v in fetch_json(MANIFEST)["versions"] if v["type"] == "release"]
    ids = [v["id"] for v in releases]
    if args.min not in ids:
        sys.exit(f"unknown version {args.min}")
    selected = list(reversed(releases[:ids.index(args.min) + 1]))  # oldest -> newest

    versions, langs, binds = [], [], []
    for entry in selected:
        lang, protocol, backend, kb = load_version(entry)
        print(f"{entry['id']:>10}  protocol {protocol:>4}  input {backend:>4}  {len(lang):>5} lang keys  {len(kb):>3} keybinds")
        versions.append({"id": entry["id"], "protocol": protocol})
        langs.append(lang)
        binds.append(kb)

    data = {
        "format": 1,
        "versions": versions,
        "lang": delta_encode(langs),
        "keybinds": delta_encode(binds),
    }
    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    raw = json.dumps(data, ensure_ascii=False, separators=(",", ":"), sort_keys=True).encode("utf-8")
    out.write_bytes(gzip.compress(raw, 9, mtime=0))
    print(f"wrote {out} ({out.stat().st_size // 1024} KB)")
    for i in (0, -1):
        print(f"default keybinds {versions[i]['id']}:", json.dumps(binds[i], indent=1, sort_keys=True))


if __name__ == "__main__":
    main()
