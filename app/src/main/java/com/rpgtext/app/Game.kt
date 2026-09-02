package com.rpgtext.app

import kotlin.math.roundToInt
import kotlin.random.Random

enum class Rarity(val weight: Int, val mult: Double) { COMMON(52,1.0), UNCOMMON(25,1.15), RARE(13,1.35), EPIC(6,1.65), LEGENDARY(3,2.05), MYTHIC(1,2.6) }
enum class AbilityKind { ATTACK, DEFENSE, HEAL, CONTROL, PASSIVE }

data class Ability(val id:Int,val name:String,val rarity:Rarity,val kind:AbilityKind,val power:Int,val cost:Int,val cooldown:Int,val text:String)
data class Skill(val name:String,val text:String,val crit:Double=0.0,val accuracy:Double=0.0,val dodge:Double=0.0,val hp:Int=0,val damage:Double=0.0,val heal:Double=0.0)
data class Gear(val slot:String,val name:String,val rarity:Rarity,val attack:Int,val defense:Int,val hp:Int,val crit:Double,val accuracy:Double)
data class Enemy(val name:String,val level:Int,val hp:Int,val maxHp:Int,val attack:Int,val defense:Int,val boss:Boolean,val trait:String)

data class Player(
    val level:Int=1,val xp:Int=0,val hp:Int=100,val maxHp:Int=100,val energy:Int=40,val maxEnergy:Int=40,
    val attack:Int=12,val defense:Int=5,val crit:Double=.05,val accuracy:Double=.90,val dodge:Double=.05,
    val abilities:List<Ability> = emptyList(),val skills:List<Skill> = emptyList(),val gear:List<Gear> = emptyList(),
    val kills:Int=0,val gold:Int=0,val damageTaken:Int=0
)

data class GameState(val player:Player,val enemy:Enemy?,val log:List<String>,val dead:Boolean=false,val victory:Boolean=false,val runId:Int=1,val floor:Int=1)

object Content {
    private val verbs = listOf("Arc","Blood","Void","Storm","Ember","Frost","Radiant","Shadow","Iron","Venom","Astral","Grave","Thunder","Solar","Moon","Rift","Wild","Crystal","Soul","Infernal","Tidal","Gale","Obsidian","Star","Dawn")
    private val nouns = listOf("Strike","Burst","Spear","Wave")
    private val descriptions = listOf("deal focused damage","deal heavy damage","pierce defense","hit with elemental force")

    val abilities:List<Ability> = List(100) { i ->
        val rarity = rollRarity(i + 17)
        val kind = AbilityKind.entries[i % AbilityKind.entries.size]
        val scale = rarity.mult
        val power = (10 + (i % 9) * 2) * scale
        val name = "${verbs[i % verbs.size]} ${nouns[i / verbs.size]}"
        val text = when(kind) {
            AbilityKind.ATTACK -> "${descriptions[i % descriptions.size].replaceFirstChar { it.uppercase() }} for ${power.roundToInt()} damage."
            AbilityKind.DEFENSE -> "Gain a barrier worth ${power.roundToInt()} for 2 turns."
            AbilityKind.HEAL -> "Restore ${power.roundToInt()} HP."
            AbilityKind.CONTROL -> "Deal ${(power*.65).roundToInt()} damage and reduce enemy attack for 2 turns."
            AbilityKind.PASSIVE -> "Passive: attacks gain ${(2 + i%7)}% bonus damage."
        }
        Ability(i,name,rarity,kind,power.roundToInt(),if(kind==AbilityKind.HEAL) 8 else 6+(i%7),if(kind==AbilityKind.PASSIVE) 0:else 2+(i%4),text)
    }

    val skills = List(40) { i ->
        when(i % 10) {
            0 -> Skill("Keen Eye ${i+1}","+${2+i%5}% accuracy",accuracy=.02+(i%5)*.01)
            1 -> Skill("Predator ${i+1}","+${2+i%5}% critical chance",crit=.02+(i%5)*.01)
            2 -> Skill("Fleet Step ${i+1}","+${2+i%5}% dodge",dodge=.02+(i%5)*.01)
            3 -> Skill("Ironblood ${i+1}","+${5+(i%5)*2} max HP",hp=5+(i%5)*2)
            4 -> Skill("Savage Force ${i+1}","+${3+i%5}% damage",damage=.03+(i%5)*.01)
            5 -> Skill("Vital Flow ${i+1}","+${3+i%5}% healing",heal=.03+(i%5)*.01)
            else -> Skill("Combat Instinct ${i+1}","+1% crit, +1% accuracy",crit=.01,accuracy=.01)
        }
    }

    fun rollRarity(seed:Int = Random.nextInt(100)):Rarity {
        var n = seed % 100
        return when { n < 52 -> Rarity.COMMON; n < 77 -> Rarity.UNCOMMON; n < 90 -> Rarity.RARE; n < 96 -> Rarity.EPIC; n < 99 -> Rarity.LEGENDARY; else -> Rarity.MYTHIC }
    }
    fun ability():Ability = abilities.random().let { it.copy(rarity=rollRarity(), power=(it.power*rollRarity().mult).roundToInt()) }
    fun gear(kill:Int):Gear {
        val r=rollRarity(kill+Random.nextInt(100)); val n=listOf("Warden","Ruin","Hunter","Oracle","Grave","Royal","Riftborn").random(); val slot=listOf("Weapon","Armor","Helm","Gloves","Boots","Relic").random()
        return Gear(slot,"${r.name.lowercase().replaceFirstChar{it.uppercase()}} $n ${slot}",r,(4+r.mult*5).roundToInt(),(2+r.mult*3).roundToInt(),(r.mult*10).roundToInt(),if(slot=="Weapon"||slot=="Relic") .01*r.weight else 0.0,.01*r.weight)
    }
    fun enemy(kill:Int):Enemy {
        val boss = kill > 0 && kill % 10 == 0
        val level = 1 + kill/3
        val growth = 1.0 + kill*0.032
        val baseHp = if(boss) 130 else 48
        val hp=(baseHp*growth*(1+level*.045)).roundToInt()
        val names=if(boss) listOf("The Ash Tyrant","Gravebound Wyrm","Crownless Devourer","Rift Colossus","Blood Oracle") else listOf("Goblin Raider","Bone Hound","Feral Wisp","Cave Stalker","Rotfang","Ashling","Bandit Marauder")
        return Enemy(names.random(),level,hp,hp,(9*growth*(if(boss) 1.35 else 1)).roundToInt(),(3+level*.65).roundToInt(),boss,listOf("Aggressive","Armored","Swift","Cursed","Regenerating").random())
    }
}

object Engine {
    fun newRun(runId:Int=Random.nextInt(100000)):GameState {
        val count=Random.nextInt(3,7)
        val abilities=Content.abilities.shuffled().take(count).map { it.copy(rarity=Content.rollRarity(), power=(it.power*it.rarity.mult).roundToInt()) }
        val skills=Content.skills.shuffled().take(Random.nextInt(1,4))
        val gear=listOf(Content.gear(0),Content.gear(0)).distinctBy{it.slot}
        val p=Player(abilities=abilities,skills=skills,gear=gear)
        return GameState(p,Content.enemy(0),listOf("Run #$runId begins. Your fate is randomized.","${abilities.size} abilities manifested.","The endless descent begins."),runId=runId)
    }

    private fun derived(p:Player):Player {
        val g=p.gear; val s=p.skills
        return p.copy(maxHp=(100+g.sumOf{it.hp}+s.sumOf{it.hp}),attack=12+g.sumOf{it.attack},defense=5+g.sumOf{it.defense},crit=(.05+g.sumOf{it.crit}+s.sumOf{it.crit}).coerceAtMost(.65),accuracy=(.90+g.sumOf{it.accuracy}+s.sumOf{it.accuracy}).coerceAtMost(.99),dodge=(.05+s.sumOf{it.dodge}).coerceAtMost(.45))
    }
    fun act(state:GameState,index:Int):GameState {
        if(state.dead || state.enemy==null) return state
        val p=derived(state.player); val e=state.enemy; val a=p.abilities.getOrNull(index) ?: return state.copy(log=(state.log+"That ability is not available.").takeLast(14))
        if(a.kind==AbilityKind.PASSIVE) return state.copy(log=(state.log+"${a.name} is passive and cannot be activated.").takeLast(14))
        if(p.energy<a.cost) return state.copy(player=p,log=(state.log+"Not enough energy for ${a.name}.").takeLast(14))
        var dmg=0; var heal=0
        when(a.kind){
            AbilityKind.ATTACK -> dmg=(a.power + p.attack*.55).roundToInt()
            AbilityKind.DEFENSE -> { heal=(a.power*.35).roundToInt(); dmg=0 }
            AbilityKind.HEAL -> heal=a.power
            AbilityKind.CONTROL -> dmg=(a.power*.65 + p.attack*.35).roundToInt()
            AbilityKind.PASSIVE -> {}
        }
        if(a.kind==AbilityKind.ATTACK && Random.nextDouble()>p.accuracy) dmg=0
        val crit= dmg>0 && Random.nextDouble()<p.crit
        if(crit)dmg=(dmg*1.75).roundToInt()
        dmg=(dmg-e.defense).coerceAtLeast(if(dmg>0)1 else 0)
        val enemyHp=(e.hp-dmg).coerceAtLeast(0)
        val healed=if(heal>0) minOf(heal,p.maxHp-p.hp) else 0
        var np=p.copy(energy=(p.energy-a.cost+8).coerceIn(0,p.maxEnergy),hp=(p.hp+healed))
        val lines=mutableListOf("You use ${a.name}. ${if(dmg>0) "-$dmg HP" else "No damage"}${if(crit) " CRITICAL!" else ""}${if(healed>0) " • +$healed HP" else ""}.")
        if(enemyHp<=0){
            val gold=8+e.level*3+(if(e.boss) 35 else 0); val loot=if(Random.nextDouble()<.72) "${Content.gear(state.floor).name}" else "${Content.ability().name}"
            val xp=12+e.level*4
            var level=np.level; var curXp=np.xp+xp
            while(curXp>=level*100){curXp-=level*100;level++}
            np=np.copy(level=level,xp=curXp,kills=np.kills+1,gold=np.gold+gold,hp=minOf(np.maxHp,np.hp+8),energy=minOf(np.maxEnergy,np.energy+10))
            val next=Content.enemy(np.kills)
            lines += "${e.name} falls. +$xp XP • +$gold gold. Loot: $loot"
            lines += if(e.boss) "BOSS SLAIN. The next foe is stronger." else "The next monster has grown stronger."
            return GameState(derived(np),next,(state.log+lines).takeLast(14),runId=state.runId,floor=state.floor+1)
        }
        var retaliation=0
        if(Random.nextDouble()>p.dodge) retaliation=(e.attack-p.defense).coerceAtLeast(1) else lines += "You evade the ${e.name}'s attack."
        np=np.copy(hp=(np.hp-retaliation).coerceAtLeast(0),damageTaken=np.damageTaken+retaliation)
        lines += if(retaliation>0) "${e.name} hits you for $retaliation." else ""
        if(np.hp<=0) return GameState(derived(np),e,(state.log+lines+"Your run ends here.").takeLast(14),dead=true,runId=state.runId,floor=state.floor)
        return GameState(derived(np),e,(state.log+lines).filter{it.isNotBlank()}.takeLast(14),runId=state.runId,floor=state.floor)
    }
}
