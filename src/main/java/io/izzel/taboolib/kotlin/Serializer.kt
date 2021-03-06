package io.izzel.taboolib.kotlin

import com.google.gson.*
import io.izzel.taboolib.module.db.local.SecuredFile
import io.izzel.taboolib.util.item.Items
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.util.*

object Serializer {

    val gson = GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().also {
        it.registerTypeHierarchyAdapter(Location::class.java, TypeLocation())
        it.registerTypeHierarchyAdapter(ItemStack::class.java, TypeItemStack())
        it.registerTypeHierarchyAdapter(SecuredFile::class.java, TypeSecuredFile())
        it.registerTypeHierarchyAdapter(YamlConfiguration::class.java, TypeYamlConfiguration())
        SerializerAdapter.map.forEach { (k, v) ->
            it.registerTypeHierarchyAdapter(k, v)
        }
    }.create()!!

    fun toJson(obj: Any): String {
        return gson.toJson(obj)
    }

    fun <T> fromJson(json: String, classOfT: Class<T>?): T {
        return gson.fromJson(json, classOfT)
    }

    @Suppress("UNCHECKED_CAST")
    fun toItemStack(data: String): ItemStack {
        ByteArrayInputStream(Base64.getDecoder().decode(data)).use { byteArrayInputStream ->
            BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
                return bukkitObjectInputStream.readObject() as ItemStack
            }
        }
    }

    fun fromItemStack(itemStack: ItemStack): String {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
                bukkitObjectOutputStream.writeObject(itemStack)
                return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun toInventory(inventory: Inventory, data: String) {
        ByteArrayInputStream(Base64.getDecoder().decode(data)).use { byteArrayInputStream ->
            BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
                val index = bukkitObjectInputStream.readObject() as Array<Int>
                index.indices.forEach {
                    inventory.setItem(index[it], bukkitObjectInputStream.readObject() as ItemStack)
                }
            }
        }
    }

    fun fromInventory(inventory: Inventory, size: Int): String {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
                (0..size).map { it to inventory.getItem(it) }.filter { Items.nonNull(it.second) }.toMap().run {
                    bukkitObjectOutputStream.writeObject(this.keys.toTypedArray())
                    this.forEach { (_, v) ->
                        bukkitObjectOutputStream.writeObject(v)
                    }
                }
            }
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
        }
    }

    class TypeLocation : JsonSerializer<Location>, JsonDeserializer<Location> {

        override fun serialize(a: Location, p1: Type, p2: JsonSerializationContext): JsonElement {
            return JsonObject().also {
                it.addProperty("world", a.world!!.name)
                it.addProperty("x", a.x)
                it.addProperty("y", a.y)
                it.addProperty("z", a.z)
                it.addProperty("yaw", a.yaw)
                it.addProperty("pitch", a.pitch)
            }
        }

        override fun deserialize(a: JsonElement, p1: Type?, p2: JsonDeserializationContext): Location {
            return Location(
                Bukkit.getWorld(a.asJsonObject.get("world").asString),
                a.asJsonObject.get("x").asDouble,
                a.asJsonObject.get("y").asDouble,
                a.asJsonObject.get("z").asDouble,
                a.asJsonObject.get("yaw").asFloat,
                a.asJsonObject.get("pitch").asFloat
            )
        }
    }

    class TypeItemStack : JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

        override fun serialize(a: ItemStack, p1: Type, p2: JsonSerializationContext): JsonElement {
            return JsonPrimitive(fromItemStack(a))
        }

        override fun deserialize(a: JsonElement, p1: Type?, p2: JsonDeserializationContext): ItemStack {
            return toItemStack(a.asString)
        }
    }

    class TypeSecuredFile : JsonSerializer<SecuredFile>, JsonDeserializer<SecuredFile> {

        override fun serialize(a: SecuredFile, p1: Type, p2: JsonSerializationContext): JsonElement {
            return JsonPrimitive(a.saveToString())
        }

        override fun deserialize(a: JsonElement, p1: Type?, p2: JsonDeserializationContext): SecuredFile {
            return SecuredFile.loadConfiguration(a.asString)
        }
    }

    class TypeYamlConfiguration : JsonSerializer<YamlConfiguration>, JsonDeserializer<YamlConfiguration> {

        override fun serialize(a: YamlConfiguration, p1: Type, p2: JsonSerializationContext): JsonElement {
            return JsonPrimitive(a.saveToString())
        }

        override fun deserialize(a: JsonElement, p1: Type?, p2: JsonDeserializationContext): YamlConfiguration {
            return SecuredFile.loadConfiguration(a.asString)
        }
    }
}