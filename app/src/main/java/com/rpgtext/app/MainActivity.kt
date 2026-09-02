package com.rpgtext.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RpgApp() }
    }
}

@Composable
fun RpgApp() {
    var state by remember { mutableStateOf(Engine.newRun()) }
    var tab by remember { mutableStateOf(0) }
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = top, bottom = bottom)
            ) {
                Header(state)
                TabBar(tab) { tab = it }
                when {
                    state.player.hp <= 0 -> DeathScreen(state) { state = Engine.newRun() }
                    tab == 0 -> CombatScreen(state) { state = it }
                    else -> BuildScreen(state)
                }
            }
        }
    }
}

@Composable
fun Header(state: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("ENDLESS DESCENT", fontWeight = FontWeight.Bold)
            Text("Depth ${state.depth}  •  Kills ${state.kills}", style = MaterialTheme.typography.labelMedium)
        }
        Text("Lv. ${state.player.level}", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TabBar(selected: Int, onSelect: (Int) -> Unit) {
    TabRow(selectedTabIndex = selected) {
        Tab(selected == 0, onClick = { onSelect(0) }, text = { Text("Combat") })
        Tab(selected == 1, onClick = { onSelect(1) }, text = { Text("Build") })
    }
}

@Composable
fun CombatScreen(state: GameState, onState: (GameState) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        EnemyCard(state)
        Spacer(Modifier.height(12.dp))
        PlayerCard(state)
        Spacer(Modifier.height(12.dp))
        AbilityPanel(state, onState)
        Spacer(Modifier.height(12.dp))
        CombatLog(state)
        state.loot?.let { loot ->
            Spacer(Modifier.height(12.dp))
            LootCard(loot) { onState(Engine.claimLoot(state)) }
        }
    }
}

@Composable
fun EnemyCard(state: GameState) {
    val e = state.enemy
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(e.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${e.trait.label}  •  Threat ${e.threat}")
            Spacer(Modifier.height(8.dp))
            Bar(e.hp, e.maxHp)
            Text("HP ${e.hp}/${e.maxHp}")
        }
    }
}

@Composable
fun PlayerCard(state: GameState) {
    val p = state.player
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("You", fontWeight = FontWeight.Bold)
                Text("${p.gold} gold")
            }
            Spacer(Modifier.height(8.dp))
            Bar(p.hp, p.maxHp)
            Text("HP ${p.hp}/${p.maxHp}  •  Energy ${p.energy}/${p.maxEnergy}")
            Text("STR ${p.str}  DEX ${p.dex}  INT ${p.intelligence}  VIT ${p.vitality}")
        }
    }
}

@Composable
fun Bar(value: Int, max: Int) {
    LinearProgressIndicator(progress = { if (max <= 0) 0f else (value.toFloat() / max).coerceIn(0f, 1f) }, Modifier.fillMaxWidth())
}

@Composable
fun AbilityPanel(state: GameState, onState: (GameState) -> Unit) {
    Text("Abilities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    state.player.abilities.forEachIndexed { index, ability ->
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(ability.name, fontWeight = FontWeight.Bold)
                    Text("${ability.rarity.label} • ${ability.description}")
                }
                if (ability.kind != AbilityKind.PASSIVE) {
                    Button(onClick = { onState(Engine.act(state, index)) }, enabled = state.loot == null && ability.cooldownRemaining == 0 && state.player.energy >= ability.energyCost) {
                        Text("Use")
                    }
                }
            }
        }
    }
    Button(onClick = { onState(Engine.finishTurn(state)) }, enabled = state.loot == null) { Text("End Turn") }
}

@Composable
fun CombatLog(state: GameState) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Combat Log", fontWeight = FontWeight.Bold)
            state.log.takeLast(8).forEach { Text("• $it") }
        }
    }
}

@Composable
fun LootCard(loot: Loot, onClaim: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Loot Found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(loot.label)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onClaim) { Text("Claim") }
        }
    }
}

@Composable
fun BuildScreen(state: GameState) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        SectionTitle("Equipment")
        state.player.gear.forEach { Text("${it.name} • ${it.description}") }
        SectionTitle("Skills")
        state.player.skills.forEach { Text("${it.name} • ${it.description}") }
        SectionTitle("Stored Drops")
        Text("Abilities: ${state.player.inventoryAbilities.size}")
        Text("Gear: ${state.player.inventoryGear.size}")
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
}

@Composable
fun DeathScreen(state: GameState, onNewRun: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("RUN OVER", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Depth ${state.depth} • ${state.kills} kills • Level ${state.player.level}")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNewRun) { Text("New Run") }
            }
        }
    }
}

private val Common = Color(0xFFB0B0B0)
private val Uncommon = Color(0xFF65D46E)
private val Rare = Color(0xFF55A8FF)
private val Epic = Color(0xFFB56BFF)
private val Legendary = Color(0xFFFFB52E)
private val Mythic = Color(0xFFFF5A78)

@Composable
fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.COMMON -> Common
    Rarity.UNCOMMON -> Uncommon
    Rarity.RARE -> Rare
    Rarity.EPIC -> Epic
    Rarity.LEGENDARY -> Legendary
    Rarity.MYTHIC -> Mythic
}
