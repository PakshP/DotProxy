package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;

import static com.dotproxy.Globals.SOURCE_URL;

public class LicenseCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("license")
            .category(CommandCategory.INFO)
            .description("Displays the software license and information about your legal rights")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("license").executes(c -> {
            c.getSource()
                .getEmbed()
                .title("DotProxy License Info")
                .description("""
                    DotProxy is licensed under [the GNU Affero General Public License](https://www.gnu.org/licenses/agpl-3.0.en.html).
                    
                    This means that you are free to use, modify, and distribute DotProxy (including hosting as a free or paid service) as long as:
                    1. You make the source code available to users
                    2. Any modifications you make to DotProxy are licensed under the AGPL
                    
                    Compiled releases bundle third party code and data which each remain licensed under their original licenses.
                    
                    Source code for DotProxy is available at [the DotProxy GitHub repository](%s).
                    """.formatted(sourceUrl()))
                .primaryColor();

        });
    }

    /**
     * If you distribute modified versions, replace Globals.SOURCE_URL with a link to your repository.
     */
    private String sourceUrl() {
        return SOURCE_URL;
    }
}
