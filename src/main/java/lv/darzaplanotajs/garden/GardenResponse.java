package lv.darzaplanotajs.garden;

import java.time.Instant;

/** Pēc hubGeometry null frontend zina, ka onboarding vēl nav pabeigts */
public record GardenResponse(Long id, String name, String icon, String color, Long addressCode, String addressLabel,
                             String hubGeometry, FrostResponse frost, Instant frostUpdatedAt,
                             String role, String ownerEmail) {

    static GardenResponse from(Garden garden, String role, String ownerEmail) {
        return new GardenResponse(garden.getId(), garden.getName(), garden.getIcon(), garden.getColor(),
                garden.getAddressCode(), garden.getAddressLabel(), garden.getHubGeometry(),
                FrostResponse.from(garden, null), garden.getFrostUpdatedAt(), role, ownerEmail);
    }
}
