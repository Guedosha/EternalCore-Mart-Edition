package com.eternalcode.core.bridge.adventure;

import com.eternalcode.commons.adventure.AdventureLegacyColorPostProcessor;
import com.eternalcode.commons.adventure.AdventureLegacyColorPreProcessor;
import com.eternalcode.commons.adventure.AdventureUrlPostProcessor;
import com.eternalcode.core.injector.annotations.Bean;
import com.eternalcode.core.injector.annotations.component.Setup;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.Plugin;

@Setup
class AdventureSetup {

    @Bean
    AudienceProvider audienceProvider(Plugin plugin) {
        return BukkitAudiences.create(plugin);
    }

    @Bean
    MiniMessage miniMessage() {
        return MiniMessage.builder()
            .postProcessor(new AdventureUrlPostProcessor())
            .postProcessor(new AdventureLegacyColorPostProcessor())
            .preProcessor(new AdventureLegacyColorPreProcessor())
            .build();
    }

}
