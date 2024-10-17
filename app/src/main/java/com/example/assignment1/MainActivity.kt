package com.example.assignment1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.assignment1.ui.theme.Assignment1Theme
import androidx.lifecycle.viewmodel.compose.viewModel


sealed class Screen(val route:String){
    data object Home: Screen("home")
    data object PlayerList: Screen("plist")
    data object TeamAchievements: Screen("accolades")
    data object FavouritePlayers: Screen("faves/{rcvdata}"){
        fun createRoute(rcvdata: String):String{
            return "faves/$rcvdata"
        }
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

data class Player(
    val name: String,
    val attributes: String,
    val imageUrl: String,
    val nationality: String,
    val shirtNo: String
)

val players = listOf(
    Player(name = "André Onana", attributes = "28, Goalkeeper", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p202641.png", nationality = "Cameroonian", shirtNo = "#24"),
    Player(name = "Diogo Dalot", attributes = "25, Defender", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p216051.png", nationality = "Portuguese", shirtNo = "#20"),
    Player(name = "Matthijis de Ligt", attributes = "25, Defender", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p209365.png", nationality = "Dutch", shirtNo = "#4"),
    Player(name = "Christian Eriksen", attributes = "32, Midfielder", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p80607.png", nationality = "Danish", shirtNo = "#14"),
    Player(name = "Kobbie Mainoo", attributes = "19, Midfielder", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p516895.png", nationality = "English", shirtNo = "#37"),
    Player(name = "Bruno Fernandes", attributes = "30, Midfielder", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p141746.png", nationality = "Portuguese", shirtNo = "#8"),
    Player(name = "Marcus Rashford", attributes = "26, Forward", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p176297.png", nationality = "English", shirtNo = "#10"),
    Player(name = "Amad Diallo", attributes = "22, Forward", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p493250.png", nationality = "Ivorian", shirtNo = "#16"),
    Player(name = "Alejandro Garnacho", attributes = "20, Forward", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p493105.png", nationality = "Argentine", shirtNo = "#17"),
    Player(name = "Rasmus Hojlund", attributes = "21, Forward", imageUrl = "https://resources.premierleague.com/premierleague/photos/players/250x250/p497894.png", nationality = "Danish", shirtNo = "#9")
)

class PlayerViewModel: ViewModel(){ //view model to pass fave from plist to favourites
    private val _favePlayers= mutableStateListOf<Player>()
    val favePlayers: List<Player> = _favePlayers

    fun addFave(player: Player){
        _favePlayers.add(player)
    }
    fun removeFave(player: Player){
        _favePlayers.remove(player)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Assignment1Theme {
                MainFunction()
            }
        }
    }
}

@Composable
fun MainFunction(){
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = viewModel() //view model instance
    //List of navbar items
    val navListItem = listOf(
        NavItem(label = "Home",icon = Icons.Default.Home,screen = Screen.Home),
        NavItem(label = "Players",icon = Icons.Default.Person,screen = Screen.PlayerList),
        NavItem(label = "Favourites",icon = Icons.Default.Star,screen = Screen.FavouritePlayers),
        NavItem(label = "Trophies",icon = Trophy,screen = Screen.TeamAchievements)
    )
    //create state var for selected index
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    // set up scaffold for bottom app bar
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar {
                NavigationBar {
                    navListItem.forEachIndexed{index,item ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = {
                                selectedIndex = index
                                if (navController.currentDestination?.route != item.screen.route){
                                    navController.navigate(item.screen.route){
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {Icon(imageVector = item.icon, contentDescription = item.label)},
                            label = {Text(text = item.label)}
                        )
                    }
                }
            }
        }){innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ){
            composable(Screen.Home.route){
                HomeScreen(navController)
            }
            composable(route = Screen.PlayerList.route){
                PlayerListScreen(navController,playerViewModel)
            }
            composable(route = Screen.TeamAchievements.route){
                TeamAchievementScreen(navController)
            }
            composable(route = Screen.FavouritePlayers.route,
                arguments = listOf(
                    navArgument("rcvdata"){type = NavType.StringType}
                )) { backStackEntry ->
                val rcvdata =
                    backStackEntry.arguments?.getString("rcvdata")
                if (rcvdata != null) {
                    FavePlayerScreen(navController, rcvdata = rcvdata, viewModel = playerViewModel)
                }
            }
        }
    }
}


@Composable
fun HomeScreen(navController: NavController){
    var text by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center){
        Image(
            painter = painterResource(id = R.drawable.ot),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween){
            Text("Manchester United Home",color = Color.Red, fontSize = 25.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        Column{
            TextField(
                value = text,
                onValueChange = {text = it},
                label = {Text(text= "Enter your name: ")}
            )
            Spacer(Modifier.padding(16.dp))
            ElevatedButton(onClick ={
                navController.navigate(Screen.PlayerList.route)
            },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)){
                Text("PlayerList")
            }
            Spacer(Modifier.padding(16.dp))

            OutlinedButton(onClick = { 
                navController.navigate(Screen.TeamAchievements.route)
            },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)){
                Text("Team Achievements", color = Color.Black)
            }
            Spacer(Modifier.padding(16.dp))

            FilledTonalButton(onClick = {
                navController.navigate(Screen.FavouritePlayers.createRoute(text))
            },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)){
                Text("Favourites", color = Color.White)
            }
            Spacer(Modifier.padding(16.dp))
            Row{
                Text("Youth.", fontWeight = FontWeight.Bold, color = Color.Red)
                Spacer(Modifier.padding(15.dp))
                Text("Courage.",fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.padding(15.dp))
                Text("Success.",fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun PlayerListScreen(navController: NavController, viewModel: PlayerViewModel){
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column{
            LazyColumn {
                items(players) { player -> // for loop iterating through list
                    PlayerCard(player, onButtonClick = {
                        viewModel.addFave(player)
                        navController.navigate(Screen.FavouritePlayers.createRoute("User"))
                    }) // passing player into card
                    Spacer(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun TeamAchievementScreen(navController: NavController){
    Box(Modifier.fillMaxSize().background(Color.Red), contentAlignment = Alignment.Center){
        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)){
            item{
                Card(Modifier.fillMaxWidth()){
                    Text("Premier Leagues Titles Won: 20", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    LoadImage("https://i.redd.it/eow0xfzkl4951.jpg")
                    Text("Years won: 1908, 1911, 1952, 1956, 1957, 1965, 1967, 1993, 1994, 1996, 1997, " +
                            "1999, 2000, 2001, 2003, 2007, 2008, 2009, 2011 and 2013", fontSize = 20.sp)
                }
            }
            item{
                Card(Modifier.fillMaxWidth()){
                    Text("Champions League Titles Won: 3",fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    LoadImage("https://imgresizer.eurosport.com/unsafe/1200x0/filters:format(jpeg)/origin-imgresizer.eurosport.com/2019/05/23/2594259-53783890-2560-1440.jpg")
                    Text("Years won: 1968, 1999, 2008", fontSize = 20.sp)
                }
            }
            item{
                Card(Modifier.fillMaxWidth()){
                    Text("Fa Cups Won: 13",fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    LoadImage("https://static.independent.co.uk/2024/05/25/17/2154615622.jpg")
                    Text("Years won: 1909, 1948, 1963, 1977, 1983, 1985, 1990, 1994, 1996, 1999, 2004, " +
                            "2016, 2024", fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun FavePlayerScreen(navController: NavController,rcvdata: String, viewModel: PlayerViewModel){
    val favePlayers = viewModel.favePlayers

    Column(Modifier.fillMaxSize().padding(16.dp)){
        Text("Welcome back $rcvdata", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.padding(16.dp))
        if (favePlayers.isEmpty()){
            Text("No favourite players yet added", fontSize = 18.sp)
        }else{
            LazyColumn {
                items(favePlayers){ player ->
                    PlayerCard(player, onButtonClick = {
                        viewModel.removeFave(player)},
                        isFaveScreen = true
                    )
                    Spacer(Modifier.padding(10.dp))
                }
            }
        }
    }
}


// code for trophy icon sourced from: https://composeicons.com/icons/material-symbols/outlined/trophy
val Trophy: ImageVector
    get() {
        if (_Trophy != null) {
            return _Trophy!!
        }
        _Trophy = ImageVector.Builder(
            name = "Trophy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(280f, 840f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(-124f)
                quadToRelative(-49f, -11f, -87.5f, -41.5f)
                reflectiveQuadTo(296f, 518f)
                quadToRelative(-75f, -9f, -125.5f, -65.5f)
                reflectiveQuadTo(120f, 320f)
                verticalLineToRelative(-40f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(200f, 200f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(400f)
                verticalLineToRelative(80f)
                horizontalLineToRelative(80f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(840f, 280f)
                verticalLineToRelative(40f)
                quadToRelative(0f, 76f, -50.5f, 132.5f)
                reflectiveQuadTo(664f, 518f)
                quadToRelative(-18f, 46f, -56.5f, 76.5f)
                reflectiveQuadTo(520f, 636f)
                verticalLineToRelative(124f)
                horizontalLineToRelative(160f)
                verticalLineToRelative(80f)
                close()
                moveToRelative(0f, -408f)
                verticalLineToRelative(-152f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(40f)
                quadToRelative(0f, 38f, 22f, 68.5f)
                reflectiveQuadToRelative(58f, 43.5f)
                moveToRelative(200f, 128f)
                quadToRelative(50f, 0f, 85f, -35f)
                reflectiveQuadToRelative(35f, -85f)
                verticalLineToRelative(-240f)
                horizontalLineTo(360f)
                verticalLineToRelative(240f)
                quadToRelative(0f, 50f, 35f, 85f)
                reflectiveQuadToRelative(85f, 35f)
                moveToRelative(200f, -128f)
                quadToRelative(36f, -13f, 58f, -43.5f)
                reflectiveQuadToRelative(22f, -68.5f)
                verticalLineToRelative(-40f)
                horizontalLineToRelative(-80f)
                close()
                moveToRelative(-200f, -52f)
            }
        }.build()
        return _Trophy!!
    }

private var _Trophy: ImageVector? = null

//Load Image from Internet Composable
@Composable
fun LoadImage(imageUrl: String){
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Player Image",
        modifier = Modifier.size(200.dp),
        contentScale = ContentScale.Fit,
        alignment = Alignment.Center
    )
}

//Create Basic Card formula
@Composable
fun PlayerCard(player: Player, onButtonClick: (Player)->Unit, isFaveScreen:Boolean = false){
    var showDetails by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxSize()
        .clickable{
            showDetails = !showDetails
        }){
        if (player.imageUrl != null){
            LoadImage(player.imageUrl)
        }else{
            Text(text = "Image Loading")//placeholder
        }
        Text(text = player.name)
        Text(text = player.attributes)
        if (showDetails){
            Text(text = player.nationality)
            Text(text = player.shirtNo)
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {

            Button(onClick = { onButtonClick(player) }) {
                if (!isFaveScreen){
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Add ${player.name} to Favourites"
                )}else{
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove ${player.name} from Favourites"
                    )}
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun Preview() {
    Assignment1Theme {
        MainFunction()
    }
}