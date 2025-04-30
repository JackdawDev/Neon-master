package dev.jackdaw1101.neon.integration;

import java.util.ArrayList;
import java.util.List;

import dev.jackdaw1101.neon.Neon;

public class IntegrationHandler {

    private final Neon plugin;
    private final List<Integration> integrations = new ArrayList<>();

    public IntegrationHandler(Neon plugin) {
        this.plugin = plugin;
    }

    public void registerIntegration(Integration integration) {
        integrations.add(integration);
        integration.register(plugin);
    }

    public void registerAll() {
        for (Integration integration : integrations) {
            integration.register(plugin);
        }
    }
}
