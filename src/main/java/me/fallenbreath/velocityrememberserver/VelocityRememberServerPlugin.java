package me.fallenbreath.velocityrememberserver;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
		id = PluginMeta.ID, name = PluginMeta.NAME, version = PluginMeta.VERSION,
		url = "https://github.com/TISUnion/VelocityRememberServer",
		description = "A velocity plugin to remember the last server you logged in",
		authors = {"Fallen_Breath"}
)
public class VelocityRememberServerPlugin
{
	private final ProxyServer server;
	private final PlayerLocations playerLocations;

	@Inject
	public VelocityRememberServerPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory)
	{
		this.server = server;
		this.playerLocations = new PlayerLocations(logger, dataDirectory.resolve("locations.yml"));
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event)
	{
		this.playerLocations.load();
		this.server.getEventManager().register(this, PlayerChooseInitialServerEvent.class, this::chooseServer);
		this.server.getEventManager().register(this, ServerConnectedEvent.class, this::recordLastServer);
	}

	private void chooseServer(PlayerChooseInitialServerEvent event)
	{
		var playerUuid = event.getPlayer().getGameProfile().getId();
		this.playerLocations.getLastServer(playerUuid).
				flatMap(this.server::getServer).
				ifPresent(event::setInitialServer);
	}

	private void recordLastServer(ServerConnectedEvent event)
	{
		this.playerLocations.setLastServer(
				event.getPlayer().getGameProfile().getId(),
				event.getServer().getServerInfo().getName()
		);
	}
}
