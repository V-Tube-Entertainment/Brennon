package com.gizmo.brennon.core.protocolize.gui.config;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.pluginsupport.PluginSupport;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.HasMessagePlaceholders;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import com.gizmo.brennon.core.pluginsupport.TritonPluginSupport;
import com.gizmo.brennon.core.protocolize.gui.utils.ItemUtils;
import com.google.common.collect.Lists;
import dev.endoy.configuration.api.ISection;
import dev.simplix.protocolize.api.chat.ChatElement;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.api.item.component.CustomModelDataComponent;
import dev.simplix.protocolize.data.ItemType;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class GuiConfigItemStack
{

    private final int data;
    private final String name;
    private final List<String> lore;
    private final String owner;
    private final List<GuiConfigEnchantment> enchantments;
    private final Integer customModelData;
    private ItemType itemType;

    public GuiConfigItemStack( final ISection section )
    {
        try
        {
            this.itemType = ItemType.valueOf( section.getString( "material" ) );
        }
        catch ( Exception e )
        {
            this.itemType = ItemType.AIR;
            BuX.getLogger().warning( "Could not find a Material for " + section.getString( "material" ) );
        }
        this.data = section.exists( "data" ) ? section.getInteger( "data" ) : 0;
        this.name = section.exists( "name" ) ? section.getString( "name" ) : "";
        this.lore = section.exists( "lores" )
            ? section.isString( "lores" ) ? Lists.newArrayList( section.getString( "lores" ) ) : section.getStringList( "lores" )
            : new ArrayList<>();
        this.owner = section.exists( "owner" ) ? section.getString( "owner" ) : null;
        this.enchantments = section.exists( "enchants" ) ? section.getSectionList( "enchants" )
            .stream()
            .map( GuiConfigEnchantment::new )
            .collect( Collectors.toList() )
            : new ArrayList<>();
        this.customModelData = section.exists( "custom-model-data" ) ? section.getInteger( "custom-model-data" ) : null;
    }

    public ItemStack buildItem( final User user )
    {
        return this.buildItem( user, MessagePlaceholders.empty() );
    }

    public ItemStack buildItem( final User user, final HasMessagePlaceholders placeholders )
    {
        if ( this.itemType == null || this.itemType == ItemType.AIR )
        {
            return new ItemStack( ItemType.AIR );
        }
        final ItemStack itemStack = new ItemStack( this.itemType );

        if ( this.data != 0 )
        {
            itemStack.durability( (short) this.data );
        }
        itemStack.lore(
            this.lore.stream()
                .flatMap( lore -> PluginSupport.getPluginSupport( TritonPluginSupport.class )
                    .map( pluginSupport -> Arrays.stream( pluginSupport.formatGuiMessage( user, lore ).split( "\n" ) ) )
                    .orElse( Stream.of( lore ) ) )
                .map( lore ->
                {
                    Component loreComponent = Utils.format( user, Utils.replacePlaceHolders( lore, placeholders ) );

                    loreComponent = loreComponent.decoration( TextDecoration.ITALIC, false );

                    return BuX.getInstance().serverOperations().getMessageComponent( loreComponent );
                } )
                .map( ChatElement::of )
                .collect( Collectors.toList() )
        );

        String itemName = PluginSupport.getPluginSupport( TritonPluginSupport.class )
            .map( pluginSupport -> pluginSupport.formatGuiMessage( user, name ) )
            .orElse( name );
        Component displayName = Utils.format( user, Utils.replacePlaceHolders( itemName, placeholders ) );
        displayName = displayName.decoration( TextDecoration.ITALIC, false );
        itemStack.displayName( ChatElement.of(
            BuX.getInstance().serverOperations().getMessageComponent( displayName )
        ) );

        this.enchantments.forEach( enchant -> enchant.addToItem( itemStack ) );

        if ( this.owner != null && !this.owner.trim().isEmpty() )
        {
            ItemUtils.setSkullTexture( itemStack, this.owner.trim() );
        }
        if ( this.customModelData != null )
        {
            itemStack.addComponent( CustomModelDataComponent.create( customModelData ) );
        }
        return itemStack;
    }
}
