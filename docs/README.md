# DotProxy Wiki

Source for the DotProxy documentation site, built with [Zensical](https://zensical.org/). Pages live in `wiki/`.

## Requirements

- [uv](https://docs.astral.sh/uv/)

## Build

`uv run zensical build`

## Serve locally

`uv run zensical serve`

`wiki/Commands.md` is generated from the command definitions by a Gradle task, so don't edit it by hand.
