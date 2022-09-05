package cc.ricecx.pvptoggle.hooks;

import cc.ricecx.pvptoggle.PvPToggle;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderApiHook extends PlaceholderExpansion {

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        if (params.equals("pvp_state")) {
            boolean state = PvPToggle.config().getPvPState(player);

            return state ? "&cOn" : "&aOff";
        }
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "PvPToggle";
    }

    @Override
    public @NotNull String getAuthor() {
        return "RiceCX";
    }

    @Override
    public @NotNull String getVersion() {
        return PvPToggle.get().getDescription().getVersion();
    }
}
