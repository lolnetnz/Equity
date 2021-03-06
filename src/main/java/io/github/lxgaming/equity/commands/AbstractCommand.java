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

import java.util.List;

public abstract class AbstractCommand {
    
    public abstract void execute(List<String> arguments);
    
    public abstract String getName();
    
    public String getDescription() {
        return "No description provided";
    }
    
    public String getUsage() {
        return null;
    }
    
    public List<String> getAliases() {
        return null;
    }
}