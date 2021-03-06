/*
 * Copyright 2017 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.equity.commands;

import io.github.lxgaming.equity.Equity;
import io.github.lxgaming.equity.util.Reference;
import io.github.lxgaming.equity.util.Toolbox;

import java.util.List;

public class InfoCommand extends AbstractCommand {
    
    @Override
    public void execute(List<String> arguments) {
        Equity.getInstance().getLogger().info("{} v{}", Reference.APP_NAME, Reference.APP_VERSION);
        Equity.getInstance().getLogger().info("Uptime: {}", Toolbox.getTimeStringFromSeconds(Toolbox.getDuration(Equity.getInstance().getStartTime()).getSeconds()));
        Equity.getInstance().getLogger().info("Authors: {}", Reference.AUTHORS);
        Equity.getInstance().getLogger().info("Source: {}", Reference.SOURCE);
        Equity.getInstance().getLogger().info("Website: {}", Reference.WEBSITE);
    }
    
    @Override
    public String getName() {
        return "Info";
    }
    
    @Override
    public String getDescription() {
        return "Shows Equity information";
    }
    
    @Override
    public List<String> getAliases() {
        return Toolbox.newArrayList("Version");
    }
}