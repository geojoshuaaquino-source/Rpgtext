package com.rpgtext.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Void = Color(0xFF07060B)
private val Panel = Color(0xFF121018)
private val Panel2 = Color(0xFF1A1721)
private val Panel3 = Color(0xFF211D29)
private val Gold = Color(0xFFFFC857)
private val Paper = Color(0xFFF5EBD7)
private val Muted = Color(0xFF9D96A8)
private val Red = Color(0xFFFF6868)
private val Blue = Color(0xFF63B9FF)
private val Green = Color(0xFF65D18B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RpgApp() }
    }
}

@Composable
fun RpgApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold,
            background = Void,
            surface = Panel,
            onSurface = Paper
        )
    ) {
        var game by remember { mutableStateOf(Engine.newRun()) }
        var tab by remember { mutableIntStateOf(0) }

        Surface(Modifier.fillMaxSize(), color = Void) {
            // Edge-to-edge is intentional; safeDrawing keeps content clear of the status/navigation bars.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Header(game)
                if (game.dead) {
                    DeathScreen(game) {
                        game = Engine.newRun()
                        tab = 0
                    }
                } else {
                    TabBar(tab) { tab = it }
                    if (tab == 0) {
                        EnemyCard(game)
                        PlayerCard(game)
                        if (game.pendingLoot != null) {
                            LootCard(game) { game = Engine.claimLoot(game) }
                        }
                        CombatLog(game.log)
                        AbilityPanel(game) { index -> game = Engine.act(game, index) }
                    } else {
                        BuildScreen(game)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun Header(g: GameState) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("ENDLESS DESCENT", color = Gold, fontSize = 21.sp, fontWeight = FontWeight.Black)
            Text("RUN #${g.runId}  •  DEPTH ${g.floor}", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ResourcePill("☠", g.player.kills.toString(), Paper)
            ResourcePill("◆", g.player.gold.toString(), Gold)
        }
    }
}

@Composable
private fun ResourcePill(icon: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Panel2)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, color = color, fontSize = 12.sp)
        Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TabBar(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Panel2)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabButton("⚔  COMBAT", selected == 0, Modifier.weight(1f)) { onSelect(0) }
        TabButton("✦  BUILD", selected == 1, Modifier.weight(1f)) { onSelect(1) }
    }
}

@Composable
private fun TabButton(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) Gold else Color.Transparent,
            contentColor = if (active) Void else Muted
        )
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun EnemyCard(g: GameState) {
    val enemy = g.enemy ?: return
    val cardColor = if (enemy.boss) Color(0xFF25121B) else Panel
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (enemy.boss) Red.copy(alpha = .35f) else Color.White.copy(alpha = .05f), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        if (enemy.boss) "♛ ${enemy.name}" else enemy.name,
                        color = if (enemy.boss) Red else Paper,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("LEVEL ${enemy.level}  •  ${enemy.trait.uppercase()}", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${enemy.hp}", color = Red, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("HP", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Bar("ENEMY VITALITY", enemy.hp, enemy.maxHp, Red)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (enemy.boss) Red.copy(alpha = .08f) else Panel2).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (enemy.boss) "BOSS" else "THREAT", color = if (enemy.boss) Red else Gold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(7.dp))
                Text(if (enemy.boss) "Elite encounter • no final boss" else "Every kill makes the next enemy stronger.", color = Muted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun PlayerCard(g: GameState) {
    val p = g.player
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ADVENTURER", color = Paper, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("SURVIVE • ADAPT • DESCEND", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Text("LV.${p.level}", color = Void, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Gold).padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
            Bar("HP", p.hp, p.maxHp, Red)
            Bar("ENERGY", p.energy, p.maxEnergy, Blue)
            Bar("XP", p.xp, p.level * 100, Gold)
            HorizontalDivider(color = Color.White.copy(alpha = .06f), modifier = Modifier.padding(vertical = 2.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("ATK", p.attack)
                Stat("DEF", p.defense)
                Stat("CRIT", "${(p.crit * 100).toInt()}%")
                Stat("ACC", "${(p.accuracy * 100).toInt()}%")
                Stat("DODGE", "${(p.dodge * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: Any) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(value.toString(), color = Paper, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text(label, color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Bar(label: String, value: Int, max: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Black)
            Text("$value/$max", color = Paper, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (value.toFloat() / max.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)),
            color = color,
            trackColor = Panel2
        )
    }
}

private fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.COMMON -> Muted
    Rarity.UNCOMMON -> Green
    Rarity.RARE -> Color(0xFF65AFFF)
    Rarity.EPIC -> Color(0xFFB27BFF)
    Rarity.LEGENDARY -> Gold
    Rarity.MYTHIC -> Color(0xFFFF6FB1)
}

@Composable
private fun AbilityPanel(g: GameState, onUse: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("ABILITIES", color = Gold, fontWeight = FontWeight.Black, fontSize = 13.sp)
                Text("Your randomized kit", color = Muted, fontSize = 9.sp)
            }
            Text("${g.player.abilities.size}/6", color = Paper, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        g.player.abilities.forEachIndexed { index, ability ->
            val rarity = rarityColor(ability.rarity)
            val cooldown = g.cooldowns[index] ?: 0
            val active = ability.kind != AbilityKind.PASSIVE
            val usable = active && cooldown == 0 && g.player.energy >= ability.cost
            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, rarity.copy(alpha = .28f), RoundedCornerShape(15.dp))
            ) {
                Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(rarity.copy(alpha = .12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (active) "✦" else "◆", color = rarity, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(ability.name, color = rarity, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        Text("${ability.rarity.name}  •  ${ability.kind.name}  •  ${ability.cost} ENERGY", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(ability.text, color = Paper, fontSize = 10.sp, lineHeight = 14.sp)
                        if (active) {
                            Text(
                                when {
                                    cooldown > 0 -> "Cooldown: $cooldown turn${if (cooldown == 1) "" else "s"}"
                                    g.player.energy < ability.cost -> "Not enough energy"
                                    else -> "READY"
                                },
                                color = when {
                                    cooldown > 0 -> Muted
                                    g.player.energy < ability.cost -> Red
                                    else -> Green
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    if (active) {
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { onUse(index) },
                            enabled = usable,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = rarity,
                                contentColor = Void,
                                disabledContainerColor = Panel2,
                                disabledContentColor = Muted
                            ),
                            contentPadding = PaddingValues(horizontal = 9.dp),
                            modifier = Modifier.width(70.dp),
                            shape = RoundedCornerShape(9.dp)
                        ) {
                            Text(if (cooldown > 0) "$cooldown" else if (g.player.energy < ability.cost) "LOW" else "USE", fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Text("AUTO", color = rarity, fontWeight = FontWeight.Black, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CombatLog(log: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0B12)), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("COMBAT LOG", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Black)
                Text("LATEST", color = Muted.copy(alpha = .65f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
            log.takeLast(6).forEach { message -> Text("› $message", fontSize = 9.sp, color = Paper) }
        }
    }
}

@Composable
private fun LootCard(g: GameState, onClaim: () -> Unit) {
    val loot = g.pendingLoot ?: return
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF211A12)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Gold.copy(alpha = .7f), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("✦ LOOT FOUND", color = Gold, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text("REWARD", color = Gold.copy(alpha = .7f), fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
            if (loot.kind == LootKind.GEAR) {
                val item = loot.gear!!
                Text(item.name, color = rarityColor(item.rarity), fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text("${item.rarity.name}  •  ${item.slot}", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("+${item.attack} ATK  •  +${item.defense} DEF  •  +${item.hp} HP  •  +${(item.crit * 100).toInt()}% CRIT", color = Paper, fontSize = 10.sp)
            } else {
                val ability = loot.ability!!
                Text(ability.name, color = rarityColor(ability.rarity), fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text("${ability.rarity.name}  •  ${ability.kind.name}", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(ability.text, color = Paper, fontSize = 10.sp, lineHeight = 14.sp)
            }
            Text("Combat is paused while you inspect this drop.", color = Muted, fontSize = 9.sp)
            Button(onClick = onClaim, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Void), modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(11.dp)) {
                Text("CLAIM & CONTINUE", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun BuildScreen(g: GameState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("EQUIPMENT", "CURRENT LOADOUT")
        g.player.gear.forEach { item ->
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(13.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(9.dp)).background(rarityColor(item.rarity).copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                        Text("◆", color = rarityColor(item.rarity), fontSize = 13.sp)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(item.name, color = rarityColor(item.rarity), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text("${item.rarity.name}  •  ${item.slot}", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("+${item.attack} ATK  •  +${item.defense} DEF  •  +${item.hp} HP", color = Paper, fontSize = 9.sp)
                    }
                    Text("+${(item.crit * 100).toInt()}% CRIT\n+${(item.accuracy * 100).toInt()}% ACC", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        SectionTitle("SKILLS", "PASSIVE BONUSES")
        g.player.skills.forEach { skill ->
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(skill.name, color = Paper, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(skill.text, color = Muted, fontSize = 9.sp, lineHeight = 13.sp)
                }
            }
        }
        SectionTitle("INVENTORY", "STORED DROPS")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InventoryPill("GEAR", g.player.inventoryGear.size, Modifier.weight(1f))
            InventoryPill("ABILITIES", g.player.inventoryAbilities.size, Modifier.weight(1f))
        }
        Text("Six abilities can be equipped. Every new run randomizes the build.", color = Muted, fontSize = 9.sp)
    }
}

@Composable
private fun SectionTitle(text: String, detail: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(text, color = Gold, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text(detail, color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InventoryPill(label: String, value: Int, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(Panel2).padding(11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value.toString(), color = Paper, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(label, color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun DeathScreen(g: GameState, restart: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Text("YOUR RUN HAS ENDED", color = Red, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("DEPTH ${g.floor}  •  ${g.player.kills} KILLS", color = Paper, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniResult("LEVEL", g.player.level.toString(), Modifier.weight(1f))
            MiniResult("GOLD", g.player.gold.toString(), Modifier.weight(1f))
            MiniResult("DAMAGE", g.player.damageTaken.toString(), Modifier.weight(1f))
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Death rerolls your abilities, skills, equipment, and run. No final boss — the descent only gets harder.", color = Muted, fontSize = 10.sp, lineHeight = 15.sp, modifier = Modifier.padding(13.dp))
        }
        Button(onClick = restart, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Void), modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Text("BEGIN NEW RUN", fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun MiniResult(label: String, value: String, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, color = Paper, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Text(label, color = Muted, fontSize = 7.sp, fontWeight = FontWeight.Black)
        }
    }
}
