package com.rpgtext.app

import kotlin.math.roundToInt
import kotlin.random.Random

enum class Rarity(val weight:Int,val mult:Double){COMMON(52,1.0),UNCOMMON(25,1.15),RARE(13,1.35),EPIC(6,1.65),LEGENDARY(3,2.05),MYTHIC(1,2.6)}
enum class AbilityKind{ATTACK,DEFENSE,HEAL,CONTROL,PASSIVE}
enum class LootKind{ABILITY,GEAR}

data class Ability(val id:Int,val name:String,val rarity:Rarity,val kind:AbilityKind,val power:Int,val cost:Int,val cooldown:Int,val text:String)
data class Skill(val name:String,val text:String,val crit:Double=0.0,val accuracy:Double=0.0,val dodge:Double=0.0,val hp:Int=0,val damage:Double=0.0,val heal:Double=0.0)
data class Gear(val slot:String,val name:String,val rarity:Rarity,val attack:Int,val defense:Int,val hp:Int,val crit:Double,val accuracy:Double)
data class Loot(val kind:LootKind,val ability:Ability?=null,val gear:Gear?=null)
data class Enemy(val name:String,val level:Int,val hp:Int,val maxHp:Int,val attack:Int,val defense:Int,val boss:Boolean,val trait:String)
data class Player(val level:Int=1,val xp:Int=0,val hp:Int=100,val maxHp:Int=100,val energy:Int=40,val maxEnergy:Int=40,val attack:Int=12,val defense:Int=5,val crit:Double=.05,val accuracy:Double=.90,val dodge:Double=.05,val abilities:List<Ability> = emptyList(),val skills:List<Skill> = emptyList(),val gear:List<Gear> = emptyList(),val inventoryAbilities:List<Ability> = emptyList(),val inventoryGear:List<Gear> = emptyList(),val kills:Int=0,val gold:Int=0,val damageTaken:Int=0)
data class GameState(val player:Player,val enemy:Enemy?,val log:List<String>,val dead:Boolean=false,val victory:Boolean=false,val runId:Int=1,val floor:Int=1,val pendingLoot:Loot?=null,val barrier:Int=0,val weakenedTurns:Int=0,val regenTurns:Int=0,val cooldowns:Map<Int,Int> = emptyMap())

object Content{
    private val verbs=listOf("Arc","Blood","Void","Storm","Ember","Frost","Radiant","Shadow","Iron","Venom","Astral","Grave","Thunder","Solar","Moon","Rift","Wild","Crystal","Soul","Infernal","Tidal","Gale","Obsidian","Star","Dawn")
    private val nouns=listOf("Strike","Burst","Spear","Wave")
    private val descriptions=listOf("Deal focused damage","Deal heavy damage","Pierce defense","Hit with elemental force")
    val abilities:List<Ability>=List(100){i->
        val r=rollRarity(i+17);val kind=AbilityKind.entries[i%AbilityKind.entries.size];val power=((10+(i%9)*2)*r.mult).roundToInt();val name="${verbs[i%verbs.size]} ${nouns[i/verbs.size]}"
        val text=when(kind){AbilityKind.ATTACK->"${descriptions[i%descriptions.size]} for $power damage.";AbilityKind.DEFENSE->"Gain a barrier worth $power and restore a little energy.";AbilityKind.HEAL->"Restore $power HP.";AbilityKind.CONTROL->"Deal ${(power*.65).roundToInt()} damage and weaken the enemy.";AbilityKind.PASSIVE->"Passive: regenerate 2 HP after each action."}
        Ability(i,name,r,kind,power,if(kind==AbilityKind.HEAL)8 else 6+i%7,if(kind==AbilityKind.PASSIVE)0 else 2+i%4,text)
    }
    val skills:List<Skill>=List(40){i->when(i%10){0->Skill("Keen Eye ${i+1}","+${2+i%5}% accuracy",accuracy=.02+(i%5)*.01);1->Skill("Predator ${i+1}","+${2+i%5}% critical chance",crit=.02+(i%5)*.01);2->Skill("Fleet Step ${i+1}","+${2+i%5}% dodge",dodge=.02+(i%5)*.01);3->Skill("Ironblood ${i+1}","+${5+(i%5)*2} max HP",hp=5+(i%5)*2);4->Skill("Savage Force ${i+1}","+${3+i%5}% damage",damage=.03+(i%5)*.01);5->Skill("Vital Flow ${i+1}","+${3+i%5}% healing",heal=.03+(i%5)*.01);else->Skill("Combat Instinct ${i+1}","+1% crit, +1% accuracy",crit=.01,accuracy=.01)}}
    fun rollRarity(seed:Int=Random.nextInt(100)):Rarity{val n=seed%100;return when{n<52->Rarity.COMMON;n<77->Rarity.UNCOMMON;n<90->Rarity.RARE;n<96->Rarity.EPIC;n<99->Rarity.LEGENDARY;else->Rarity.MYTHIC}}
    fun ability():Ability{val a=abilities.random();val r=rollRarity();return a.copy(id=Random.nextInt(1_000_000),rarity=r,power=(a.power*r.mult).roundToInt())}
    fun gear(kill:Int):Gear{val r=rollRarity(kill+Random.nextInt(100));val n=listOf("Warden","Ruin","Hunter","Oracle","Grave","Royal","Riftborn").random();val slot=listOf("Weapon","Armor","Helm","Gloves","Boots","Relic").random();val tier=r.mult;return Gear(slot,"${r.name.lowercase().replaceFirstChar{it.uppercase()}} $n $slot",r,(4+tier*5).roundToInt(),(2+tier*3).roundToInt(),(tier*10).roundToInt(),if(slot=="Weapon"||slot=="Relic")(.01+tier*.012).coerceAtMost(.06) else 0.0,(.005+tier*.01).coerceAtMost(.035))}
    fun enemy(kill:Int):Enemy{val boss=kill>0&&kill%10==0;val level=1+kill/3;val growth=1.0+kill*.032;val base=if(boss)130 else 48;val hp=(base*growth*(1+level*.045)).roundToInt();val names=if(boss)listOf("The Ash Tyrant","Gravebound Wyrm","Crownless Devourer","Rift Colossus","Blood Oracle")else listOf("Goblin Raider","Bone Hound","Feral Wisp","Cave Stalker","Rotfang","Ashling","Bandit Marauder");return Enemy(names.random(),level,hp,hp,(9*growth*(if(boss)1.35 else 1)).roundToInt(),(3+level*.65).roundToInt(),boss,listOf("Aggressive","Armored","Swift","Cursed","Regenerating").random())}
}

object Engine{
    fun newRun(runId:Int=Random.nextInt(100000)):GameState{
        val count=Random.nextInt(3,7)
        var abilities=Content.abilities.shuffled().take(count).map{val r=Content.rollRarity();it.copy(id=Random.nextInt(1_000_000),rarity=r,power=(it.power*r.mult).roundToInt())}
        if(abilities.none{it.kind!=AbilityKind.PASSIVE}){val active=Content.abilities.filter{it.kind!=AbilityKind.PASSIVE}.random();abilities=abilities.dropLast(1)+active.copy(id=Random.nextInt(1_000_000),rarity=Content.rollRarity())}
        val skills=Content.skills.shuffled().take(Random.nextInt(1,4))
        val gear=buildStartingGear()
        val p=Player(abilities=abilities,skills=skills,gear=gear)
        return GameState(derived(p),Content.enemy(0),listOf("Run #$runId begins. Your build is randomized.","$count abilities manifested.","The endless descent begins."),runId=runId)
    }
    private fun buildStartingGear():List<Gear>{val slots=listOf("Weapon","Armor","Helm","Gloves","Boots","Relic").shuffled().take(2);return slots.map{slot->Content.gear(0).copy(slot=slot,name="${Content.rollRarity().name.lowercase().replaceFirstChar{it.uppercase()}} Starter $slot")}}
    private fun derived(p:Player):Player{val g=p.gear;val s=p.skills;val damageMult=1+s.sumOf{it.damage};return p.copy(maxHp=100+g.sumOf{it.hp}+s.sumOf{it.hp},attack=((12+g.sumOf{it.attack})*damageMult).roundToInt(),defense=5+g.sumOf{it.defense},crit=(.05+g.sumOf{it.crit}).coerceAtMost(.65),accuracy=(.90+g.sumOf{it.accuracy}).coerceAtMost(.99),dodge=(.05+s.sumOf{it.dodge}).coerceAtMost(.45))}
    }
    private fun gearScore(x:Gear)=x.rarity.mult+x.attack*.01+x.defense*.01+x.hp*.005+x.crit*2+x.accuracy
    fun claimLoot(state:GameState):GameState{val loot=state.pendingLoot?:return state;val p=state.player;return when(loot.kind){LootKind.GEAR->{val item=loot.gear!!;val old=p.gear.find{it.slot==item.slot};val better=old==null||gearScore(item)>gearScore(old);if(better)state.copy(player=derived(p.copy(gear=p.gear.filterNot{it.slot==item.slot}+item)),pendingLoot=null,log=(state.log+"Equipped ${item.name}.").takeLast(14))else state.copy(player=p.copy(inventoryGear=p.inventoryGear+item),pendingLoot=null,log=(state.log+"Stored ${item.name} in inventory.").takeLast(14))};LootKind.ABILITY->{val a=loot.ability!!;val updated=if(p.abilities.size<6)p.abilities+a else p.abilities;state.copy(player=p.copy(abilities=updated.take(6),inventoryAbilities=p.inventoryAbilities+a.let{listOf(it)}),pendingLoot=null,log=(state.log+"${a.name} joined your ability pool.").takeLast(14))}}
    }
    fun act(state:GameState,index:Int):GameState{
        if(state.dead||state.enemy==null||state.pendingLoot!=null)return state
        val p=derived(state.player);val e=state.enemy;val a=p.abilities.getOrNull(index)?:return state
        val remaining=state.cooldowns[index]?:0
        if(a.kind==AbilityKind.PASSIVE)return state.copy(log=(state.log+"${a.name} is passive; its effect triggers automatically.").takeLast(14))
        if(remaining>0)return state.copy(log=(state.log+"${a.name} is on cooldown for $remaining more turn${if(remaining==1)""else"s"}.").takeLast(14))
        if(p.energy<a.cost)return state.copy(log=(state.log+"Not enough energy for ${a.name}.").takeLast(14))
        var dmg=0;var heal=0;when(a.kind){AbilityKind.ATTACK->dmg=(a.power+p.attack*.55).roundToInt();AbilityKind.DEFENSE->{};AbilityKind.HEAL->heal=a.power;AbilityKind.CONTROL->dmg=(a.power*.65+p.attack*.35).roundToInt();AbilityKind.PASSIVE->{}}
        if((a.kind==AbilityKind.ATTACK||a.kind==AbilityKind.CONTROL)&&Random.nextDouble()>p.accuracy)dmg=0
        val crit=dmg>0&&Random.nextDouble()<p.crit;if(crit)dmg=(dmg*1.75).roundToInt();dmg=(dmg-e.defense).coerceAtLeast(if(dmg>0)1 else 0)
        val enemyHp=(e.hp-dmg).coerceAtLeast(0);val healMult=1+p.skills.sumOf{it.heal};val healed=(heal*healMult).roundToInt().coerceAtMost(p.maxHp-p.hp)
        var np=p.copy(energy=(p.energy-a.cost+8).coerceIn(0,p.maxEnergy),hp=p.hp+healed)
        val lines=mutableListOf("You use ${a.name}. ${if(dmg>0)"-$dmg HP"else"No damage"}${if(crit)" CRITICAL!"else""}${if(healed>0)" • +$healed HP"else""}.")
        var weakened=state.weakenedTurns
        val newCooldowns=state.cooldowns.toMutableMap()
        if(a.cooldown>0)newCooldowns[index]=a.cooldown+1
        if(a.kind==AbilityKind.DEFENSE){val barrier=(a.power*.75).roundToInt();return finishTurn(state,np,e,lines,barrier,weakened,newCooldowns)}
        if(a.kind==AbilityKind.CONTROL)weakened=2
        if(enemyHp<=0){val gold=8+e.level*3+if(e.boss)35 else 0;val xp=12+e.level*4;var level=np.level;var curXp=np.xp+xp;while(curXp>=level*100){curXp-=level*100;level++};np=np.copy(level=level,xp=curXp,kills=np.kills+1,gold=np.gold+gold,hp=minOf(np.maxHp,np.hp+8),energy=minOf(np.maxEnergy,np.energy+10));val loot=if(Random.nextDouble()<.62)Loot(LootKind.GEAR,gear=Content.gear(state.player.kills))else Loot(LootKind.ABILITY,ability=Content.ability());return GameState(derived(np),Content.enemy(np.kills),(state.log+lines+"${e.name} falls. +$xp XP • +$gold gold."+(if(e.boss)" BOSS SLAIN. The endless descent continues." else " The next monster has grown stronger.")).takeLast(14),runId=state.runId,floor=state.floor+1,pendingLoot=loot,barrier=0,weakenedTurns=0,cooldowns=emptyMap())}
        var retaliation=(e.attack-p.defense).coerceAtLeast(1)
        when(e.trait){"Aggressive"->retaliation=(retaliation*1.15).roundToInt().coerceAtLeast(1);"Armored"->{};"Swift"->{if(Random.nextDouble()<.15)retaliation+=(retaliation*.35).roundToInt()};"Cursed"->if(Random.nextDouble()<.20){np=np.copy(energy=(np.energy-3).coerceAtLeast(0));lines+="The curse drains 3 energy."};"Regenerating"->{} }
        if(weakened>0)retaliation=(retaliation*.65).roundToInt().coerceAtLeast(1)
        if(Random.nextDouble()<p.dodge)retaliation=0
        var remainingBarrier=state.barrier
        if(remainingBarrier>0&&retaliation>0){val blocked=minOf(remainingBarrier,retaliation);retaliation-=blocked;remainingBarrier-=blocked;lines+="Barrier blocks $blocked damage."}
        if(retaliation==0)lines+="You evade the ${e.name}'s attack."else lines+="${e.name} hits you for $retaliation."
        if(e.trait=="Regenerating"&&enemyHp>0){val regen=minOf(3+e.level/4,e.maxHp-enemyHp);if(regen>0)lines+="${e.name} regenerates $regen HP."}
        val finalEnemyHp=if(e.trait=="Regenerating")minOf(e.maxHp,enemyHp+3+e.level/4)else enemyHp
        np=np.copy(hp=(np.hp-retaliation).coerceAtLeast(0),damageTaken=np.damageTaken+retaliation)
        return finishTurn(state,np,e.copy(hp=finalEnemyHp),lines,remainingBarrier,weakened,newCooldowns)
    }
    private fun finishTurn(state:GameState,np:Player,e:Enemy,lines:MutableList<String>,barrier:Int,weakened:Int,cooldowns:Map<Int,Int>):GameState{
        var p=np
        if(p.abilities.any{it.kind==AbilityKind.PASSIVE}){val regen=2.coerceAtMost(p.maxHp-p.hp);if(regen>0){p=p.copy(hp=p.hp+regen);lines+="Your passive regeneration restores $regen HP."}}
        val nextCooldowns=cooldowns.mapValues{(_,v)->(v-1).coerceAtLeast(0)}.filterValues{it>0}
        if(p.hp<=0)return GameState(derived(p),e,(state.log+lines+"Your run ends here.").takeLast(14),dead=true,runId=state.runId,floor=state.floor,cooldowns=emptyMap())
        return GameState(derived(p),e,(state.log+lines).takeLast(14),runId=state.runId,floor=state.floor,barrier=barrier,weakenedTurns=(weakened-1).coerceAtLeast(0),cooldowns=nextCooldowns)
    }
}