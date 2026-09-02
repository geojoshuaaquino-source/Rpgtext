package com.rpgtext.app

import kotlin.math.roundToInt
import kotlin.random.Random

enum class Rarity(val weight: Int, val mult: Double) {
    COMMON(52, 1.0), UNCOMMON(25, 1.15), RARE(13, 1.35),
    EPIC(6, 1.65), LEGENDARY(3, 2.05), MYTHIC(1, 2.60)
}

enum class AbilityKind { ATTACK, DEFENSE, HEAL, CONTROL, PASSIVE }
enum class LootKind { ABILITY, GEAR }

data class Ability(
    val id: Int,
    val name: String,
    val rarity: Rarity,
    val kind: AbilityKind,
    val power: Int,
    val cost: Int,
    val cooldown: Int,
    val text: String
)

data class Skill(
    val name: String,
    val text: String,
    val crit: Double = 0.0,
    val accuracy: Double = 0.0,
    val dodge: Double = 0.0,
    val hp: Int = 0,
    val damage: Double = 0.0,
    val heal: Double = 0.0
)

data class Gear(
    val slot: String,
    val name: String,
    val rarity: Rarity,
    val attack: Int,
    val defense: Int,
    val hp: Int,
    val crit: Double,
    val accuracy: Double
)

data class Loot(
    val kind: LootKind,
    val ability: Ability? = null,
    val gear: Gear? = null
)

data class Enemy(
    val name: String,
    val level: Int,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val boss: Boolean,
    val trait: String
)

data class Player(
    val level: Int = 1,
    val xp: Int = 0,
    val hp: Int = 100,
    val maxHp: Int = 100,
    val energy: Int = 40,
    val maxEnergy: Int = 40,
    val attack: Int = 12,
    val defense: Int = 5,
    val crit: Double = 0.05,
    val accuracy: Double = 0.90,
    val dodge: Double = 0.05,
    val abilities: List<Ability> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val gear: List<Gear> = emptyList(),
    val inventoryAbilities: List<Ability> = emptyList(),
    val inventoryGear: List<Gear> = emptyList(),
    val kills: Int = 0,
    val gold: Int = 0,
    val damageTaken: Int = 0
)

data class GameState(
    val player: Player,
    val enemy: Enemy?,
    val log: List<String>,
    val dead: Boolean = false,
    val victory: Boolean = false,
    val runId: Int = 1,
    val floor: Int = 1,
    val pendingLoot: Loot? = null,
    val barrier: Int = 0,
    val weakenedTurns: Int = 0,
    val cooldowns: Map<Int, Int> = emptyMap()
)

object Content {
    private val verbs = listOf(
        "Arc", "Blood", "Void", "Storm", "Ember", "Frost", "Radiant", "Shadow",
        "Iron", "Venom", "Astral", "Grave", "Thunder", "Solar", "Moon", "Rift",
        "Wild", "Crystal", "Soul", "Infernal", "Tidal", "Gale", "Obsidian", "Star", "Dawn"
    )
    private val nouns = listOf("Strike", "Burst", "Spear", "Wave")

    fun rollRarity(): Rarity {
        return when (Random.nextInt(100)) {
            in 0..51 -> Rarity.COMMON
            in 52..76 -> Rarity.UNCOMMON
            in 77..89 -> Rarity.RARE
            in 90..95 -> Rarity.EPIC
            in 96..98 -> Rarity.LEGENDARY
            else -> Rarity.MYTHIC
        }
    }

    val abilities: List<Ability> = List(100) { i ->
        val rarity = rollRarity()
        val kind = AbilityKind.entries[i % AbilityKind.entries.size]
        val power = ((10 + (i % 9) * 2) * rarity.mult).roundToInt()
        val name = "${verbs[i % verbs.size]} ${nouns[i / verbs.size]}"
        val text = when (kind) {
            AbilityKind.ATTACK -> "Deal $power damage."
            AbilityKind.DEFENSE -> "Gain a $power-point barrier."
            AbilityKind.HEAL -> "Restore $power HP."
            AbilityKind.CONTROL -> "Deal ${(power * 0.65).roundToInt()} damage and weaken the enemy."
            AbilityKind.PASSIVE -> "Regenerate 2 HP after each action."
        }
        Ability(
            id = i,
            name = name,
            rarity = rarity,
            kind = kind,
            power = power,
            cost = if (kind == AbilityKind.HEAL) 8 else 6 + i % 7,
            cooldown = if (kind == AbilityKind.PASSIVE) 0 else 2 + i % 4,
            text = text
        )
    }

    val skills: List<Skill> = List(40) { i ->
        when (i % 7) {
            0 -> Skill("Keen Eye ${i + 1}", "+${2 + i % 5}% accuracy", accuracy = 0.02 + (i % 5) * 0.01)
            1 -> Skill("Predator ${i + 1}", "+${2 + i % 5}% critical chance", crit = 0.02 + (i % 5) * 0.01)
            2 -> Skill("Fleet Step ${i + 1}", "+${2 + i % 5}% dodge", dodge = 0.02 + (i % 5) * 0.01)
            3 -> Skill("Ironblood ${i + 1}", "+${5 + (i % 5) * 2} max HP", hp = 5 + (i % 5) * 2)
            4 -> Skill("Savage Force ${i + 1}", "+${3 + i % 5}% damage", damage = 0.03 + (i % 5) * 0.01)
            5 -> Skill("Vital Flow ${i + 1}", "+${3 + i % 5}% healing", heal = 0.03 + (i % 5) * 0.01)
            else -> Skill("Combat Instinct ${i + 1}", "+1% crit and accuracy", crit = 0.01, accuracy = 0.01)
        }
    }

    fun ability(): Ability {
        val base = abilities.random()
        val rarity = rollRarity()
        return base.copy(
            id = Random.nextInt(1_000_000),
            rarity = rarity,
            power = (base.power * rarity.mult).roundToInt()
        )
    }

    fun gear(kills: Int): Gear {
        val rarity = rollRarity()
        val slot = listOf("Weapon", "Armor", "Helm", "Gloves", "Boots", "Relic").random()
        val prefix = listOf("Warden", "Ruin", "Hunter", "Oracle", "Grave", "Royal", "Riftborn").random()
        val tier = rarity.mult + kills * 0.01
        return Gear(
            slot = slot,
            name = "${rarity.name.lowercase().replaceFirstChar { it.uppercase() }} $prefix $slot",
            rarity = rarity,
            attack = (4 + tier * 5).roundToInt(),
            defense = (2 + tier * 3).roundToInt(),
            hp = (tier * 10).roundToInt(),
            crit = if (slot == "Weapon" || slot == "Relic") (0.01 + tier * 0.012).coerceAtMost(0.06) else 0.0,
            accuracy = (0.005 + tier * 0.01).coerceAtMost(0.035)
        )
    }

    fun enemy(kills: Int): Enemy {
        val boss = kills > 0 && kills % 10 == 0
        val level = 1 + kills / 3
        val growth = 1.0 + kills * 0.032
        val baseHp = if (boss) 130 else 48
        val hp = (baseHp * growth * (1.0 + level * 0.045)).roundToInt()
        val names = if (boss) {
            listOf("The Ash Tyrant", "Gravebound Wyrm", "Crownless Devourer", "Rift Colossus", "Blood Oracle")
        } else {
            listOf("Goblin Raider", "Bone Hound", "Feral Wisp", "Cave Stalker", "Rotfang", "Ashling", "Bandit Marauder")
        }
        return Enemy(
            name = names.random(),
            level = level,
            hp = hp,
            maxHp = hp,
            attack = (9 * growth * if (boss) 1.35 else 1.0).roundToInt(),
            defense = (3 + level * 0.65).roundToInt(),
            boss = boss,
            trait = listOf("Aggressive", "Armored", "Swift", "Cursed", "Regenerating").random()
        )
    }
}

object Engine {
    fun newRun(runId: Int = Random.nextInt(100000)): GameState {
        val abilityCount = Random.nextInt(3, 7)
        var abilities = Content.abilities.shuffled().take(abilityCount).map { base ->
            val rarity = Content.rollRarity()
            base.copy(
                id = Random.nextInt(1_000_000),
                rarity = rarity,
                power = (base.power * rarity.mult).roundToInt()
            )
        }
        if (abilities.none { it.kind != AbilityKind.PASSIVE }) {
            val active = Content.abilities.filter { it.kind != AbilityKind.PASSIVE }.random()
            abilities = abilities.dropLast(1) + active.copy(id = Random.nextInt(1_000_000), rarity = Content.rollRarity())
        }
        val skills = Content.skills.shuffled().take(Random.nextInt(1, 4))
        val gear = listOf("Weapon", "Armor").map { slot -> Content.gear(0).copy(slot = slot) }
        val player = Player(abilities = abilities, skills = skills, gear = gear)
        return GameState(
            player = derived(player),
            enemy = Content.enemy(0),
            log = listOf("Run #$runId begins.", "$abilityCount abilities manifested.", "The endless descent begins."),
            runId = runId
        )
    }

    private fun derived(player: Player): Player {
        val gearAttack = player.gear.sumOf { it.attack }
        val gearDefense = player.gear.sumOf { it.defense }
        val gearHp = player.gear.sumOf { it.hp }
        val gearCrit = player.gear.sumOf { it.crit }
        val gearAccuracy = player.gear.sumOf { it.accuracy }
        val skillDamage = player.skills.sumOf { it.damage }
        return player.copy(
            maxHp = 100 + gearHp + player.skills.sumOf { it.hp },
            attack = ((12 + gearAttack) * (1.0 + skillDamage)).roundToInt(),
            defense = 5 + gearDefense,
            crit = (0.05 + gearCrit + player.skills.sumOf { it.crit }).coerceAtMost(0.65),
            accuracy = (0.90 + gearAccuracy + player.skills.sumOf { it.accuracy }).coerceAtMost(0.99),
            dodge = (0.05 + player.skills.sumOf { it.dodge }).coerceAtMost(0.45)
        )
    }

    private fun gearScore(gear: Gear): Double {
        return gear.rarity.mult + gear.attack * 0.01 + gear.defense * 0.01 + gear.hp * 0.005 + gear.crit * 2 + gear.accuracy
    }

    fun claimLoot(state: GameState): GameState {
        val loot = state.pendingLoot ?: return state
        val player = state.player
        return when (loot.kind) {
            LootKind.GEAR -> {
                val item = loot.gear ?: return state.copy(pendingLoot = null)
                val old = player.gear.find { it.slot == item.slot }
                val better = old == null || gearScore(item) > gearScore(old)
                if (better) {
                    val newGear = player.gear.filterNot { it.slot == item.slot } + item
                    state.copy(
                        player = derived(player.copy(gear = newGear)),
                        pendingLoot = null,
                        log = (state.log + "Equipped ${item.name}.").takeLast(14)
                    )
                } else {
                    state.copy(
                        player = player.copy(inventoryGear = player.inventoryGear + item),
                        pendingLoot = null,
                        log = (state.log + "Stored ${item.name} in inventory.").takeLast(14)
                    )
                }
            }
            LootKind.ABILITY -> {
                val ability = loot.ability ?: return state.copy(pendingLoot = null)
                val equipped = if (player.abilities.size < 6) player.abilities + ability else player.abilities
                state.copy(
                    player = player.copy(
                        abilities = equipped,
                        inventoryAbilities = player.inventoryAbilities + ability
                    ),
                    pendingLoot = null,
                    log = (state.log + if (player.abilities.size < 6) "${ability.name} equipped." else "${ability.name} stored.").takeLast(14)
                )
            }
        }
    }

    fun act(state: GameState, index: Int): GameState {
        if (state.dead || state.enemy == null || state.pendingLoot != null) return state
        val player = derived(state.player)
        val enemy = state.enemy
        val ability = player.abilities.getOrNull(index) ?: return state
        if (ability.kind == AbilityKind.PASSIVE) {
            return state.copy(log = (state.log + "${ability.name} is passive; it triggers automatically.").takeLast(14))
        }
        val cooldown = state.cooldowns[index] ?: 0
        if (cooldown > 0) {
            return state.copy(log = (state.log + "${ability.name} has $cooldown turn(s) remaining.").takeLast(14))
        }
        if (player.energy < ability.cost) {
            return state.copy(log = (state.log + "Not enough energy for ${ability.name}.").takeLast(14))
        }

        var damage = when (ability.kind) {
            AbilityKind.ATTACK -> (ability.power + player.attack * 0.55).roundToInt()
            AbilityKind.CONTROL -> (ability.power * 0.65 + player.attack * 0.35).roundToInt()
            else -> 0
        }
        var healed = 0
        if (ability.kind == AbilityKind.HEAL) {
            healed = (ability.power * (1.0 + player.skills.sumOf { it.heal })).roundToInt()
            healed = healed.coerceAtMost(player.maxHp - player.hp)
        }
        if (damage > 0 && Random.nextDouble() > player.accuracy) damage = 0
        val critical = damage > 0 && Random.nextDouble() < player.crit
        if (critical) damage = (damage * 1.75).roundToInt()
        if (enemy.trait == "Armored") damage = (damage * 0.80).roundToInt()
        damage = if (damage > 0) (damage - enemy.defense).coerceAtLeast(1) else 0

        var nextPlayer = player.copy(
            energy = (player.energy - ability.cost + 8).coerceIn(0, player.maxEnergy),
            hp = (player.hp + healed).coerceAtMost(player.maxHp)
        )
        val lines = mutableListOf(
            "You use ${ability.name}. ${if (damage > 0) "-$damage HP" else "No damage"}${if (critical) " CRITICAL!" else ""}${if (healed > 0) " • +$healed HP" else ""}."
        )
        val nextCooldowns = state.cooldowns.toMutableMap()
        if (ability.cooldown > 0) nextCooldowns[index] = ability.cooldown
        val weakened = if (ability.kind == AbilityKind.CONTROL) 2 else state.weakenedTurns
        val enemyHp = (enemy.hp - damage).coerceAtLeast(0)

        if (ability.kind == AbilityKind.DEFENSE) {
            val newBarrier = (ability.power * 0.75).roundToInt()
            return finishTurn(state, nextPlayer, enemy, lines, newBarrier, weakened, nextCooldowns)
        }

        if (enemyHp <= 0) {
            val xp = 12 + enemy.level * 4
            val gold = 8 + enemy.level * 3 + if (enemy.boss) 35 else 0
            var level = nextPlayer.level
            var currentXp = nextPlayer.xp + xp
            while (currentXp >= level * 100) {
                currentXp -= level * 100
                level++
            }
            nextPlayer = nextPlayer.copy(
                level = level,
                xp = currentXp,
                kills = nextPlayer.kills + 1,
                gold = nextPlayer.gold + gold,
                hp = (nextPlayer.hp + 8).coerceAtMost(nextPlayer.maxHp),
                energy = (nextPlayer.energy + 10).coerceAtMost(nextPlayer.maxEnergy)
            )
            val loot = if (Random.nextDouble() < 0.62) {
                Loot(LootKind.GEAR, gear = Content.gear(nextPlayer.kills))
            } else {
                Loot(LootKind.ABILITY, ability = Content.ability())
            }
            val message = "${enemy.name} falls. +$xp XP • +$gold gold."
            return GameState(
                player = derived(nextPlayer),
                enemy = Content.enemy(nextPlayer.kills),
                log = (state.log + lines + message + if (enemy.boss) " BOSS SLAIN. No final boss awaits." else " The next monster is stronger.").takeLast(14),
                runId = state.runId,
                floor = state.floor + 1,
                pendingLoot = loot,
                barrier = 0,
                weakenedTurns = 0,
                cooldowns = emptyMap()
            )
        }

        var retaliation = (enemy.attack - player.defense).coerceAtLeast(1)
        when (enemy.trait) {
            "Aggressive" -> retaliation = (retaliation * 1.15).roundToInt().coerceAtLeast(1)
            "Swift" -> if (Random.nextDouble() < 0.15) retaliation += (retaliation * 0.35).roundToInt()
            "Cursed" -> if (Random.nextDouble() < 0.20) {
                nextPlayer = nextPlayer.copy(energy = (nextPlayer.energy - 3).coerceAtLeast(0))
                lines += "The curse drains 3 energy."
            }
        }
        if (weakened > 0) retaliation = (retaliation * 0.65).roundToInt().coerceAtLeast(1)
        if (Random.nextDouble() < player.dodge) retaliation = 0

        var remainingBarrier = state.barrier
        if (remainingBarrier > 0 && retaliation > 0) {
            val blocked = minOf(remainingBarrier, retaliation)
            retaliation -= blocked
            remainingBarrier -= blocked
            lines += "Barrier blocks $blocked damage."
        }
        if (retaliation == 0) lines += "You evade the ${enemy.name}'s attack."
        else lines += "${enemy.name} hits you for $retaliation."

        var finalEnemyHp = enemyHp
        if (enemy.trait == "Regenerating" && finalEnemyHp > 0) {
            val regen = minOf(3 + enemy.level / 4, enemy.maxHp - finalEnemyHp)
            finalEnemyHp += regen
            if (regen > 0) lines += "${enemy.name} regenerates $regen HP."
        }
        nextPlayer = nextPlayer.copy(
            hp = (nextPlayer.hp - retaliation).coerceAtLeast(0),
            damageTaken = nextPlayer.damageTaken + retaliation
        )
        return finishTurn(
            state,
            nextPlayer,
            enemy.copy(hp = finalEnemyHp),
            lines,
            remainingBarrier,
            weakened,
            nextCooldowns
        )
    }

    private fun finishTurn(
        state: GameState,
        player: Player,
        enemy: Enemy,
        lines: MutableList<String>,
        barrier: Int,
        weakened: Int,
        cooldowns: Map<Int, Int>
    ): GameState {
        var nextPlayer = player
        if (nextPlayer.abilities.any { it.kind == AbilityKind.PASSIVE }) {
            val regen = minOf(2, nextPlayer.maxHp - nextPlayer.hp)
            if (regen > 0) {
                nextPlayer = nextPlayer.copy(hp = nextPlayer.hp + regen)
                lines += "Passive regeneration restores $regen HP."
            }
        }
        val nextCooldowns = cooldowns
            .mapValues { (_, value) -> (value - 1).coerceAtLeast(0) }
            .filterValues { it > 0 }
        if (nextPlayer.hp <= 0) {
            return GameState(
                player = derived(nextPlayer),
                enemy = enemy,
                log = (state.log + lines + "Your run ends here.").takeLast(14),
                dead = true,
                runId = state.runId,
                floor = state.floor,
                cooldowns = emptyMap()
            )
        }
        return GameState(
            player = derived(nextPlayer),
            enemy = enemy,
            log = (state.log + lines).takeLast(14),
            runId = state.runId,
            floor = state.floor,
            barrier = barrier,
            weakenedTurns = (weakened - 1).coerceAtLeast(0),
            cooldowns = nextCooldowns
        )
    }
}
