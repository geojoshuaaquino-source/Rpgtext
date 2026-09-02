package com.rpgtext.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Void=Color(0xFF07060B); private val Panel=Color(0xFF13111A); private val Panel2=Color(0xFF1B1723); private val Gold=Color(0xFFFFC857); private val Paper=Color(0xFFF5EBD7); private val Muted=Color(0xFF9D96A8); private val Red=Color(0xFFFF6868); private val Blue=Color(0xFF63B9FF)

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{RpgApp()}}}

@Composable fun RpgApp(){MaterialTheme(colorScheme=androidx.compose.material3.darkColorScheme(primary=Gold,background=Void,surface=Panel,onSurface=Paper)){var game by remember{mutableStateOf(Engine.newRun())};var tab by remember{mutableIntStateOf(0)};Surface(Modifier.fillMaxSize(),color=Void){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=14.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Header(game);if(game.dead)DeathScreen(game){game=Engine.newRun();tab=0}else{TabBar(tab){tab=it};if(tab==0){EnemyCard(game);PlayerCard(game);if(game.pendingLoot!=null)LootCard(game){game=Engine.claimLoot(game)};CombatLog(game.log);AbilityPanel(game){i->game=Engine.act(game,i)}}else BuildScreen(game)};Spacer(Modifier.height(8.dp))}}}}

@Composable private fun Header(g:GameState){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text("ENDLESS DESCENT",color=Gold,fontSize=21.sp,fontWeight=FontWeight.Black);Text("RUN #${g.runId}  •  DEPTH ${g.floor}",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Medium)};Column(horizontalAlignment=Alignment.End){Text("☠ ${g.player.kills}",color=Paper,fontWeight=FontWeight.Bold);Text("◆ ${g.player.gold}",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold)}}}

@Composable private fun TabBar(selected:Int,onSelect:(Int)->Unit){Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel2).padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){TabButton("COMBAT",selected==0,Modifier.weight(1f)){onSelect(0)};TabButton("BUILD",selected==1,Modifier.weight(1f)){onSelect(1)}}}
@Composable private fun TabButton(label:String,active:Boolean,modifier:Modifier,onClick:()->Unit){Button(onClick=onClick,modifier=modifier.height(40.dp),shape=RoundedCornerShape(10.dp),colors=ButtonDefaults.buttonColors(containerColor=if(active)Gold else Color.Transparent,contentColor=if(active)Void else Muted),elevation=ButtonDefaults.buttonElevation(defaultElevation=0.dp)){Text(label,fontSize=11.sp,fontWeight=FontWeight.Black)}}

@Composable private fun EnemyCard(g:GameState){val e=g.enemy?:return;Card(colors=CardDefaults.cardColors(containerColor=if(e.boss)Color(0xFF25121B)else Panel),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Top){Column(Modifier.weight(1f)){Text(if(e.boss)"♛ ${e.name}"else e.name,fontSize=19.sp,fontWeight=FontWeight.Bold,color=if(e.boss)Red else Paper);Text("LEVEL ${e.level}  •  ${e.trait.uppercase()}",color=Muted,fontSize=10.sp,fontWeight=FontWeight.Bold)};Text("${e.hp}/${e.maxHp}",color=Red,fontWeight=FontWeight.Black)};LinearProgressIndicator(progress={e.hp.toFloat()/e.maxHp},modifier=Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(8.dp)),color=Red,trackColor=Color(0xFF39252C));Text(if(e.boss)"BOSS ENCOUNTER  •  THE DESCENT NEVER ENDS"else"Threat rises with every kill.",color=Muted,fontSize=10.sp)}}}

@Composable private fun PlayerCard(g:GameState){val p=g.player;Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("ADVENTURER",fontWeight=FontWeight.Black);Text("LV.${p.level}",color=Gold,fontWeight=FontWeight.Black)};Bar("HP",p.hp,p.maxHp,Red);Bar("ENERGY",p.energy,p.maxEnergy,Blue);Bar("XP",p.xp,p.level*100,Gold);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Stat("ATK",p.attack);Stat("DEF",p.defense);Stat("CRIT","${(p.crit*100).toInt()}%");Stat("DODGE","${(p.dodge*100).toInt()}%")};if(p.inventoryGear.isNotEmpty()||p.inventoryAbilities.isNotEmpty())Text("INVENTORY  ${p.inventoryGear.size+p.inventoryAbilities.size}",color=Muted,fontSize=9.sp,fontWeight=FontWeight.Bold)}}}

@Composable private fun Stat(label:String,value:Any){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(value.toString(),color=Paper,fontSize=12.sp,fontWeight=FontWeight.Bold);Text(label,color=Muted,fontSize=8.sp)}}
@Composable private fun Bar(label:String,value:Int,max:Int,color:Color){Column(verticalArrangement=Arrangement.spacedBy(3.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label,color=Muted,fontSize=9.sp,fontWeight=FontWeight.Bold);Text("$value/$max",color=Paper,fontSize=9.sp)};LinearProgressIndicator(progress={(value.toFloat()/max).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)),color=color,trackColor=Panel2)}}

private fun rarityColor(r:Rarity)=when(r){Rarity.COMMON->Muted;Rarity.UNCOMMON->Color(0xFF65D18B);Rarity.RARE->Color(0xFF65AFFF);Rarity.EPIC->Color(0xFFB27BFF);Rarity.LEGENDARY->Gold;Rarity.MYTHIC->Color(0xFFFF6FB1)}

@Composable private fun AbilityPanel(g:GameState,onUse:(Int)->Unit){Column(verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("ABILITIES",color=Gold,fontWeight=FontWeight.Black,fontSize=13.sp);Text("${g.player.abilities.size}/6 EQUIPPED",color=Muted,fontSize=9.sp,fontWeight=FontWeight.Bold)};g.player.abilities.forEachIndexed { i,a -> val rc=rarityColor(a.rarity);val usable=g.pendingLoot==null&&g.player.energy>=a.cost&&a.kind!=AbilityKind.PASSIVE;Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().border(1.dp,rc.copy(alpha=.35f),RoundedCornerShape(14.dp))){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){Text(a.name,color=rc,fontWeight=FontWeight.Bold);Text("${a.rarity.name}  •  ${a.kind.name}  •  ${a.cost} ENERGY",color=Muted,fontSize=9.sp,fontWeight=FontWeight.Bold);Text(a.text,color=Paper,fontSize=11.sp)};Spacer(Modifier.width(8.dp));if(a.kind!=AbilityKind.PASSIVE)Button(onClick={onUse(i)},enabled=usable,colors=ButtonDefaults.buttonColors(containerColor=rc,contentColor=Void,disabledContainerColor=Panel2,disabledContentColor=Muted),modifier=Modifier.width(78.dp),shape=RoundedCornerShape(10.dp)){Text(if(g.player.energy<a.cost)"LOW"else"USE",fontSize=10.sp,fontWeight=FontWeight.Black)}else Text("AUTO",color=rc,fontWeight=FontWeight.Black,fontSize=10.sp)}}}}}}

@Composable private fun CombatLog(log:List<String>){Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF0D0B12)),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text("COMBAT LOG",color=Muted,fontSize=9.sp,fontWeight=FontWeight.Black);log.takeLast(6).forEach{Text("› $it",fontSize=10.sp,color=Paper)}}}}

@Composable private fun LootCard(g:GameState,onClaim:()->Unit){val loot=g.pendingLoot?:return;Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF211A12)),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().border(1.dp,Gold.copy(alpha=.65f),RoundedCornerShape(18.dp))){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("✦ LOOT FOUND",color=Gold,fontWeight=FontWeight.Black,fontSize=14.sp);if(loot.kind==LootKind.GEAR){val x=loot.gear!!;Text(x.name,color=rarityColor(x.rarity),fontSize=18.sp,fontWeight=FontWeight.Black);Text("${x.rarity.name}  •  ${x.slot}",color=Muted,fontSize=10.sp,fontWeight=FontWeight.Bold);Text("+${x.attack} ATK  •  +${x.defense} DEF  •  +${x.hp} HP",color=Paper,fontSize=11.sp)}else{val x=loot.ability!!;Text(x.name,color=rarityColor(x.rarity),fontSize=18.sp,fontWeight=FontWeight.Black);Text("${x.rarity.name}  •  ${x.kind.name}",color=Muted,fontSize=10.sp,fontWeight=FontWeight.Bold);Text(x.text,color=Paper,fontSize=11.sp)};Text("Combat is paused until you claim the drop.",color=Muted,fontSize=9.sp);Button(onClick=onClaim,colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Void),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(11.dp)){Text("CLAIM & CONTINUE",fontWeight=FontWeight.Black)}}}}

@Composable private fun BuildScreen(g:GameState){Column(verticalArrangement=Arrangement.spacedBy(10.dp)){SectionTitle("EQUIPMENT");g.player.gear.forEach{item->Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(12.dp),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(item.name,color=rarityColor(item.rarity),fontWeight=FontWeight.Bold);Text(item.rarity.name,color=Muted,fontSize=9.sp,fontWeight=FontWeight.Bold)};Column(horizontalAlignment=Alignment.End){Text("+${item.attack} ATK",color=Paper,fontSize=10.sp);Text("+${item.defense} DEF  •  +${item.hp} HP",color=Muted,fontSize=9.sp)}}}};SectionTitle("SKILLS");g.player.skills.forEach{skill->Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(11.dp)){Text(skill.name,color=Paper,fontWeight=FontWeight.Bold,fontSize=12.sp);Text(skill.text,color=Muted,fontSize=10.sp)}}};SectionTitle("STORED DROPS");Text("Gear: ${g.player.inventoryGear.size}  •  Ability drops: ${g.player.inventoryAbilities.size}",color=Paper,fontSize=11.sp);Text("Six abilities can be equipped. Randomization stays active every run.",color=Muted,fontSize=10.sp)}}

@Composable private fun SectionTitle(text:String){Text(text,color=Gold,fontWeight=FontWeight.Black,fontSize=13.sp)}

@Composable private fun DeathScreen(g:GameState,restart:()->Unit){Column(Modifier.fillMaxWidth().padding(top=35.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("YOUR RUN HAS ENDED",color=Red,fontSize=27.sp,fontWeight=FontWeight.Black);Text("DEPTH ${g.floor}  •  ${g.player.kills} KILLS",color=Paper,fontWeight=FontWeight.Bold);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){MiniResult("LEVEL",g.player.level.toString(),Modifier.weight(1f));MiniResult("GOLD",g.player.gold.toString(),Modifier.weight(1f));MiniResult("DAMAGE",g.player.damageTaken.toString(),Modifier.weight(1f))};Text("Death rerolls abilities, skills, equipment, enemies, and the entire run.",color=Muted,fontSize=11.sp);Button(onClick=restart,colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Void),modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp)){Text("BEGIN NEW RUN",fontWeight=FontWeight.Black)}}}

@Composable private fun MiniResult(label:String,value:String,modifier:Modifier){Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(12.dp),modifier=modifier){Column(Modifier.padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(value,color=Paper,fontWeight=FontWeight.Black);Text(label,color=Muted,fontSize=8.sp)}}}
