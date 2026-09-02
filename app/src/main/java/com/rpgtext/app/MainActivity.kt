package com.rpgtext.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Void=Color(0xFF07060B); private val Panel=Color(0xFF13111A); private val Panel2=Color(0xFF1B1723); private val Gold=Color(0xFFFFC857); private val Paper=Color(0xFFF5EBD7); private val Muted=Color(0xFF9D96A8); private val Red=Color(0xFFFF6868)

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{RpgApp()}}}

@Composable fun RpgApp(){MaterialTheme(colorScheme=androidx.compose.material3.darkColorScheme(primary=Gold,background=Void,surface=Panel,onSurface=Paper)){var game by remember{mutableStateOf(Engine.newRun())};Surface(Modifier.fillMaxSize(),color=Void){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Header(game);if(game.dead)DeathScreen(game){game=Engine.newRun()}else{EnemyCard(game);PlayerCard(game);if(game.pendingLoot!=null)LootCard(game){game=Engine.claimLoot(game)};CombatLog(game.log);AbilityPanel(game){i->game=Engine.act(game,i)};Inventory(game)}}}}}

@Composable fun Header(g:GameState){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("ENDLESS DESCENT",color=Gold,fontSize=21.sp,fontWeight=FontWeight.Black);Text("RUN #${g.runId} • DEPTH ${g.floor}",color=Muted,fontSize=11.sp)};Column(horizontalAlignment=androidx.compose.ui.Alignment.End){Text("☠ ${g.player.kills}",color=Paper,fontWeight=FontWeight.Bold);Text("◆ ${g.player.gold}",color=Gold,fontSize=11.sp)}}}

@Composable fun EnemyCard(g:GameState){val e=g.enemy?:return;Card(colors=CardDefaults.cardColors(containerColor=if(e.boss)Color(0xFF25121B)else Panel),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text(if(e.boss)"♛ ${e.name}"else e.name,fontSize=19.sp,fontWeight=FontWeight.Bold,color=if(e.boss)Red else Paper);Text("Lv.${e.level} • ${e.trait}",color=Muted,fontSize=12.sp)};Text("${e.hp}/${e.maxHp}",color=Red,fontWeight=FontWeight.Bold)};LinearProgressIndicator({e.hp.toFloat()/e.maxHp},Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(8.dp)),color=Red,trackColor=Color(0xFF39252C));Text(if(e.boss)"BOSS • Endless run — no final boss."else"Every kill raises the threat level.",color=Muted,fontSize=11.sp)}}}

@Composable fun PlayerCard(g:GameState){val p=g.player;Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("ADVENTURER",fontWeight=FontWeight.Bold);Text("Lv.${p.level}",color=Gold,fontWeight=FontWeight.Bold)};Bar("HP",p.hp,p.maxHp,Red);Bar("ENERGY",p.energy,p.maxEnergy,Color(0xFF63B9FF));Bar("XP",p.xp,p.level*100,Gold);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("ATK ${p.attack}",color=Muted,fontSize=12.sp);Text("DEF ${p.defense}",color=Muted,fontSize=12.sp);Text("CRIT ${(p.crit*100).toInt()}%",color=Muted,fontSize=12.sp);Text("DODGE ${(p.dodge*100).toInt()}%",color=Muted,fontSize=12.sp)}}}}

@Composable fun Bar(label:String,v:Int,max:Int,c:Color){Column{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label,color=Muted,fontSize=10.sp);Text("$v/$max",fontSize=10.sp)};LinearProgressIndicator({(v.toFloat()/max).coerceIn(0f,1f)},Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)),color=c,trackColor=Panel2)}}

private fun rarityColor(r:Rarity)=when(r){Rarity.COMMON->Muted;Rarity.UNCOMMON->Color(0xFF65D18B);Rarity.RARE->Color(0xFF65AFFF);Rarity.EPIC->Color(0xFFB27BFF);Rarity.LEGENDARY->Gold;Rarity.MYTHIC->Color(0xFFFF6FB1)}

@Composable fun AbilityPanel(g:GameState,onUse:(Int)->Unit){Column(verticalArrangement=Arrangement.spacedBy(7.dp)){Text("ABILITIES • ${g.player.abilities.size}/6",color=Gold,fontWeight=FontWeight.Bold,fontSize=13.sp);g.player.abilities.forEachIndexed{i,a->{val rc=rarityColor(a.rarity);Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().border(1.dp,rc.copy(alpha=.35f),RoundedCornerShape(14.dp))){Row(Modifier.padding(12.dp)){Column(Modifier.weight(1f)){Text(a.name,color=rc,fontWeight=FontWeight.Bold);Text("${a.rarity.name} • ${a.kind.name} • ${a.cost} energy",color=Muted,fontSize=10.sp);Text(a.text,color=Paper,fontSize=12.sp)};Spacer(Modifier.width(8.dp));if(a.kind!=AbilityKind.PASSIVE)Button(onClick={onUse(i)},colors=ButtonDefaults.buttonColors(containerColor=rc,contentColor=Void),modifier=Modifier.width(76.dp)){Text("USE",fontWeight=FontWeight.Bold)}else Text("AUTO",color=rc,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=12.dp))}}}}}}

@Composable fun LootCard(g:GameState,onClaim:()->Unit){val loot=g.pendingLoot?:return;Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF211A12)),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().border(1.dp,Gold.copy(alpha=.55f),RoundedCornerShape(18.dp))){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("✦ LOOT FOUND",color=Gold,fontWeight=FontWeight.Black,fontSize=14.sp);if(loot.kind==LootKind.GEAR){val x=loot.gear!!;Text(x.name,color=rarityColor(x.rarity),fontSize=18.sp,fontWeight=FontWeight.Bold);Text("${x.rarity.name} • ${x.slot}",color=Muted,fontSize=11.sp);Text("+${x.attack} ATK • +${x.defense} DEF • +${x.hp} HP",color=Paper,fontSize=12.sp)}else{val x=loot.ability!!;Text(x.name,color=rarityColor(x.rarity),fontSize=18.sp,fontWeight=FontWeight.Bold);Text("${x.rarity.name} • ${x.kind.name}",color=Muted,fontSize=11.sp);Text(x.text,color=Paper,fontSize=12.sp)};Button(onClick=onClaim,colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Void),modifier=Modifier.fillMaxWidth()){Text("CLAIM LOOT",fontWeight=FontWeight.Black)}}}}

@Composable fun CombatLog(log:List<String>){Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF0D0B12)),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(3.dp)){Text("COMBAT LOG",color=Muted,fontSize=10.sp,fontWeight=FontWeight.Bold);log.takeLast(7).forEach{Text("› $it",fontSize=11.sp,color=Paper)}}}}

@Composable fun Inventory(g:GameState){Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Text("RUN BUILD",color=Gold,fontWeight=FontWeight.Bold,fontSize=13.sp);Text("Skills • ${g.player.skills.joinToString(" • "){it.name}}",color=Muted,fontSize=11.sp);g.player.gear.forEach{Text("${it.slot}: ${it.name} • +${it.attack} ATK • +${it.defense} DEF • +${it.hp} HP",color=Paper,fontSize=11.sp)};if(g.player.inventoryGear.isNotEmpty())Text("Stored gear: ${g.player.inventoryGear.size}",color=Muted,fontSize=10.sp);if(g.player.inventoryAbilities.isNotEmpty())Text("Stored ability drops: ${g.player.inventoryAbilities.size}",color=Muted,fontSize=10.sp)}}

@Composable fun DeathScreen(g:GameState,restart:()->Unit){Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)){Spacer(Modifier.height(30.dp));Text("YOUR RUN HAS ENDED",color=Red,fontSize=26.sp,fontWeight=FontWeight.Black);Text("Depth ${g.floor} • ${g.player.kills} kills • ${g.player.gold} gold",color=Paper);Text("Death rerolls abilities, skills, equipment, and the entire run.",color=Muted,fontSize=12.sp);Button(onClick=restart,colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Void),modifier=Modifier.fillMaxWidth()){Text("BEGIN NEW RUN",fontWeight=FontWeight.Black)}}}
