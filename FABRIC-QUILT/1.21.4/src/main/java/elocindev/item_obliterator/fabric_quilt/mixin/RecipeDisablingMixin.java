package elocindev.item_obliterator.fabric_quilt.mixin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import elocindev.item_obliterator.fabric_quilt.ItemObliterator;
import net.minecraft.recipe.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import elocindev.item_obliterator.fabric_quilt.util.Utils;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerRecipeManager.class)
public class RecipeDisablingMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/JsonDataLoader;load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/resource/ResourceFinder;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V", shift = At.Shift.AFTER), method = "prepare")
    private void item_obliterator$prepare(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<?> ci, @Local LocalRef<SortedMap<Identifier, Recipe<?>>> sortedMap) {
        AtomicInteger acc = new AtomicInteger();
        SortedMap<Identifier, Recipe<?>> filtered = new TreeMap<>();
        sortedMap.get().forEach(((identifier, recipe) -> {
            try {
                // Should probably find a better way to get recipe result. This can break with a lot of recipes. (Mostly transmute)
                var resultID = recipe.craft(null, null).getRegistryEntry().getIdAsString();
                ItemObliterator.LOGGER.info(resultID);
                if (!Utils.shouldRecipeBeDisabled(resultID)) {
                    filtered.put(identifier, recipe);
                }
            } catch (NullPointerException e) {
                ItemObliterator.LOGGER.debug("Failed to parse recipe {}", identifier);
                acc.getAndIncrement();
            }
        }));
        if (acc.intValue() > 0) {
            ItemObliterator.LOGGER.warn("Failed to parse {} recipe(s)", acc);
        }
        ItemObliterator.LOGGER.info(String.valueOf(sortedMap.get().size()));
        sortedMap.set(filtered);
        ItemObliterator.LOGGER.info(String.valueOf(sortedMap.get().size()));
    }
}