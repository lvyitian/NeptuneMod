/*
 * This file is part of NeptuneVanilla, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015-2017, Jamie Mansfield <https://github.com/jamierocks>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.neptunepowered.vanilla.commandsys;

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.PlayerSelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

public class NeptunePlayerSelector implements PlayerSelector {

    @Override
    public Player matchOnePlayer(MessageReceiver caller, String pattern) {
        return (Player) net.minecraft.command.PlayerSelector.matchOnePlayer((ICommandSender) caller, pattern);
    }

    @Override
    public Player[] matchPlayers(MessageReceiver caller, String pattern) {
        List<? extends EntityPlayerMP> matches = net.minecraft.command.PlayerSelector
                .matchEntities((ICommandSender) caller, pattern, EntityPlayerMP.class);
        return matches.toArray(new Player[matches.size()]);
    }

    @Override
    public boolean matchesMultiplePlayers(String pattern) {
        return net.minecraft.command.PlayerSelector.matchesMultiplePlayers(pattern);
    }

}
