package me.fallenbreath.velocityrememberserver;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerLocations
{
	// uuid -> serverName
	private final Map<String, String> locations = Collections.synchronizedMap(Maps.newLinkedHashMap());

	private final Logger logger;
	private final Path locationFilePath;

	public PlayerLocations(Logger logger, Path locationFilePath)
	{
		this.logger = logger;
		this.locationFilePath = locationFilePath;
	}

	public void load()
	{
		this.locations.clear();

		if (!this.locationFilePath.toFile().exists())
		{
			try
			{
				var dir = this.locationFilePath.getParent().toFile();
				if (!dir.exists() && !dir.mkdir())
				{
					throw new IOException(String.format("Create directory %s failed", dir));
				}
				Files.writeString(this.locationFilePath, "locations: {}");
			}
			catch (IOException e)
			{
				this.logger.error("Failed to create default location file", e);
				return;
			}
		}

		try
		{
			String yamlContent = Files.readString(this.locationFilePath);
			var locations = new Yaml().loadAs(yamlContent, Map.class).get("locations");
			((Map<?, ?>)locations).forEach((k, v) -> {
				if (k instanceof String && v instanceof String)
				{
					this.locations.put(k.toString(), v.toString());
				}
			});
		}
		catch (Exception e)
		{
			this.logger.error("Failed to load player locations", e);
		}
	}

	public void save()
	{
		try
		{
			DumperOptions dumperOptions = new DumperOptions();
			dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			String yamlContent = new Yaml(dumperOptions).dump(this.locations);
			Files.writeString(this.locationFilePath, yamlContent, StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			this.logger.error("Failed to save player locations", e);
		}
	}

	public Optional<String> getLastServer(UUID playerUuid)
	{
		return Optional.ofNullable(this.locations.get(playerUuid.toString()));
	}

	public void setLastServer(UUID playerUuid, String serverName)
	{
		this.locations.put(playerUuid.toString(), serverName);
		this.save();
	}
}
