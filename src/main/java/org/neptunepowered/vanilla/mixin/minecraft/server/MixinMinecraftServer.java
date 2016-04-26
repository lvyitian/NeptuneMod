/*
 * This file is part of NeptuneVanilla, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015-2016, Jamie Mansfield <https://github.com/jamierocks>
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
package org.neptunepowered.vanilla.mixin.minecraft.server;

import com.google.common.collect.Lists;
import net.canarymod.Canary;
import net.canarymod.api.CommandBlockLogic;
import net.canarymod.api.ConfigurationManager;
import net.canarymod.api.OfflinePlayer;
import net.canarymod.api.PlayerListData;
import net.canarymod.api.PlayerReference;
import net.canarymod.api.Server;
import net.canarymod.api.chat.ChatComponent;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.gui.GUIControl;
import net.canarymod.api.inventory.recipes.CraftingRecipe;
import net.canarymod.api.inventory.recipes.Recipe;
import net.canarymod.api.inventory.recipes.SmeltRecipe;
import net.canarymod.api.world.World;
import net.canarymod.api.world.WorldManager;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.config.Configuration;
import net.canarymod.tasks.ServerTask;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import org.neptunepowered.vanilla.wrapper.inventory.recipes.NeptuneRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server {

    @Shadow public long[] tickTimeArray;
    @Shadow private int tickCounter;
    @Shadow private boolean serverRunning;
    @Shadow private ServerConfigurationManager serverConfigManager;
    @Shadow private ServerStatusResponse statusResponse;

    @Shadow
    public abstract void initiateShutdown();

    @Shadow
    public abstract String getMinecraftVersion();

    @Shadow
    public abstract String[] getAllUsernames();

    @Override
    @Shadow
    public abstract String getHostname();

    @Override
    public int getNumPlayersOnline() {
        return serverConfigManager.getCurrentPlayerCount();
    }

    @Override
    @Overwrite
    public int getMaxPlayers() {
        return Configuration.getServerConfig().getMaxPlayers();
    }

    @Override
    public String[] getPlayerNameList() {
        return serverConfigManager.getAllUsernames();
    }

    @Override
    public String[] getKnownPlayerNames() {
        return this.getAllUsernames();
    }

    @Override
    public List<Player> getPlayerList() {
        return (List) this.serverConfigManager.getPlayerList();
    }

    @Override
    public String getDefaultWorldName() {
        return Configuration.getServerConfig().getDefaultWorldName();
    }

    @Override
    public WorldManager getWorldManager() {
        return null;
    }

    @Override
    public boolean consoleCommand(String command) {
        return false;
    }

    @Override
    public boolean consoleCommand(String command, Player player) {
        return false;
    }

    @Override
    public boolean consoleCommand(String command, CommandBlockLogic cmdBlockLogic) {
        return false;
    }

    @Override
    public void executeVanillaCommand(MessageReceiver caller, String command) {

    }

    @Override
    public void setTimer(String uniqueName, int time) {

    }

    @Override
    public boolean isTimerExpired(String uniqueName) {
        return false;
    }

    @Override
    public Player matchPlayer(String player) {
        return null;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String player) {
        return null;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        return null;
    }

    @Override
    public PlayerReference matchKnownPlayer(String player) {
        return null;
    }

    @Override
    public PlayerReference matchKnownPlayer(UUID uuid) {
        return null;
    }

    @Override
    public Player getPlayer(String player) {
        return null;
    }

    @Override
    public Player getPlayerFromUUID(String uuid) {
        return null;
    }

    @Override
    public Player getPlayerFromUUID(UUID uuid) {
        return null;
    }

    @Override
    public void broadcastMessage(String message) {

    }

    @Override
    public void broadcastMessageToOps(String message) {

    }

    @Override
    public void broadcastMessageToAdmins(String message) {

    }

    @Override
    public boolean loadWorld(String name, long seed) {
        return false;
    }

    @Override
    public World getWorld(String name) {
        return null;
    }

    @Override
    public World getDefaultWorld() {
        return null;
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return (ConfigurationManager) serverConfigManager;
    }

    @Override
    public void initiateShutdown(String message) {
        initiateShutdown();
    }

    @Override
    public void restart(boolean reloadCanary) {

    }

    @Override
    public boolean isRunning() {
        return serverRunning;
    }

    @Override
    public Recipe addRecipe(CraftingRecipe recipe) {
        return null;
    }

    @Override
    public List<Recipe> getServerRecipes() {
        List<Recipe> recipes = Lists.newArrayList();

        for (Object recipe : CraftingManager.getInstance().getRecipeList()) {
            recipes.add(NeptuneRecipe.of((IRecipe) recipe));
        }

        return recipes;
    }

    @Override
    public boolean removeRecipe(Recipe recipe) {
        return false;
    }

    @Override
    public void addSmeltingRecipe(SmeltRecipe recipe) {

    }

    @Override
    public List<SmeltRecipe> getServerSmeltRecipes() {
        return null;
    }

    @Override
    public boolean removeSmeltRecipe(SmeltRecipe recipe) {
        return false;
    }

    @Override
    public void addGUI(GUIControl gui) {

    }

    @Override
    public long[] getTickTimeArray() {
        return tickTimeArray;
    }

    @Override
    public String getCanaryModVersion() {
        return Canary.getImplementationVersion();
    }

    @Override
    public String getServerVersion() {
        return getMinecraftVersion();
    }

    @Override
    public int getProtocolVersion() {
        return this.statusResponse.getProtocolVersionInfo().getProtocol();
    }

    @Override
    public String getServerGUILog() {
        return null;
    }

    @Override
    public GUIControl getCurrentGUI() {
        return null;
    }

    @Override
    public boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }

    @Override
    public boolean addSynchronousTask(ServerTask task) {
        return false;
    }

    @Override
    public boolean removeSynchronousTask(ServerTask task) {
        return false;
    }

    @Override
    public void sendPlayerListData(PlayerListData data) {

    }

    @Override
    public int getCurrentTick() {
        return tickCounter;
    }

    @Override
    public float getTicksPerSecond() {
        return 0;
    }

    @Override
    public void showTitle(ChatComponent title) {

    }

    @Override
    public void showTitle(ChatComponent title, ChatComponent subtitle) {

    }

    @Override
    @Shadow
    public abstract String getName();

    @Override
    public String getLocale() {
        return Configuration.getServerConfig().getServerLocale();
    }

    @Overwrite
    public String getServerModName() {
        return "NeptuneVanilla";
    }
}