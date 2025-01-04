package com.testapp

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil.compose.SubcomposeAsyncImage
import com.project_fusion.service.ApiState
import com.testapp.pojo.ArticlesItem
import com.testapp.pojo.NewsHeadlinesPojo
import com.testapp.service.convertDate
import com.testapp.ui.theme.TestAppTheme
import com.testapp.viewmodels.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel:ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestAppTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val navControllers = rememberNavController()

                  NavHost(navController = navControllers,startDestination = NewsList,
                      modifier = Modifier.padding(innerPadding)){

                      composable<NewsList>{
                          NewsListScreen(viewModel){ selectedNewsData->
                              navControllers.navigate(ScreenB(selectedNewsData.url!!,""))
                          }
                      }

                      composable<ScreenB>(
                          exitTransition = {
                           slideOutOfContainer(
                                  animationSpec = tween(200, easing = EaseOut),
                                  towards = AnimatedContentTransitionScope.SlideDirection.End
                              )
                          }){
                          val args = it.toRoute<ScreenB>()
                          Screen2(args)
                      }
                  }

                }
            }
        }


        viewModel.callCategoryApi()
    }
}

@Serializable
object  NewsList

@Serializable
data class ScreenB(var name:String,var age:String)


@Composable
fun NewsListScreen(viewModel: ViewModel,onClick:(selectedItem:ArticlesItem)->Unit){

    val context = LocalContext.current
    val activity = context as? MainActivity

    val newsList by viewModel._newsListData.collectAsStateWithLifecycle()

    when(newsList){

        is ApiState.Loading -> {

            loaderNewsHeadlines()

        }

        is ApiState.Success -> {
            val data  = (newsList as ApiState.Success<NewsHeadlinesPojo>).data
            
            Column(modifier = Modifier.fillMaxSize()) {

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically){

                    Text(text = context.getString(R.string.headings)
                    , fontSize = 20.sp,
                        fontFamily = customFonts,
                        fontWeight = FontWeight.SemiBold)

                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.wrapContentHeight()) {

                    items(data.articles.size -1){ index ->

                        val articels = data.articles[index]


                        NewsHeadlinesCell(articels,index == data.articles.size -1){ item ->
                            onClick(item)
                        }
                    }

                }

            }
        }
        else ->{
            Column(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = context.getString(R.string.headings), fontSize = 20.sp,
                        fontFamily = customFonts,
                        fontWeight = FontWeight.SemiBold
                    )

                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    Image(painter = painterResource(id = R.drawable.no_internet_img),
                        contentDescription ="network error home" )

                    Text(
                        text = context.getString(R.string.error), fontSize = 20.sp,
                        fontFamily = customFonts,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    
                    Button(modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                        onClick = {
                        viewModel.callCategoryApi()
                    }) {
                        Text(text = context.getString(R.string.retry)
                            , fontSize = 14.sp,
                            fontFamily = customFonts,
                            fontWeight = FontWeight.SemiBold)
                    }

                }

            }
        }
    }
}

@Composable
fun Screen2(data:ScreenB){

    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            isLoading = newProgress < 100
                        }
                    }
                    loadUrl(data.name)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(35.dp)
                , color = Color.Gray
            )
        }

    }
}

@Composable
fun loaderNewsHeadlines(){
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
//
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = context.getString(R.string.headings), fontSize = 20.sp,
                fontFamily = customFonts,
                fontWeight = FontWeight.SemiBold
            )

        }

        Spacer(modifier = Modifier.height(10.dp))


        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()){
            items(5){

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 17.dp, vertical = 5.dp)){

                    Row(modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween) {

                        Row {
                            Text(
                                text = "",
                                modifier = Modifier
                                    .width(100.dp)
                                    .shimmerEffect(),
                                fontSize = 13.sp,
                                fontFamily = customFonts,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Text(
                            text = "",
                            modifier = Modifier
                                .width(100.dp)
                                .shimmerEffect(),
                            fontSize = 13.sp,
                            fontFamily = customFonts,
                            fontWeight = FontWeight.Normal
                        )

                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp), shape = RoundedCornerShape(15.dp)
                    ) {

                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .shimmerEffect()) {

                        }

                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(modifier = Modifier
                        .width(150.dp)
                        .shimmerEffect(),
                        text = "",
                        fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(modifier = Modifier
                        .width(250.dp)
                        .shimmerEffect(),
                        text = "",
                        fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                }

            }
        }

    }

}


@Composable
fun NewsHeadlinesCell(data: ArticlesItem,isLastIndex:Boolean,onClick: (selectedItem: ArticlesItem) -> Unit){

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 17.dp, vertical = 5.dp)
        .clickable {
            onClick(data)
        }){


                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween) {

                    Row(modifier = Modifier.width((screenWidth * 0.7f))) {
                        if(data.author != "" && data.author != null) {
                            Text(
                                text = "Author: ",
                                fontSize = 13.sp,
                                fontFamily = customFonts,
                                fontWeight = FontWeight.Normal
                            )

                            Text(
                                text = data.author,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = customFonts,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = convertDate(data.publishedAt ?:""),
                        fontSize = 13.sp,
                        fontFamily = customFonts,
                        fontWeight = FontWeight.Normal
                    )
            }

        Spacer(modifier = Modifier.height(10.dp))

        Card(modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
            shape = RoundedCornerShape(size = 15.dp)) {

            SubcomposeAsyncImage(
                model = data.urlToImage,
                contentDescription = "Loaded Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                        contentAlignment = Alignment.Center){

                        Image(modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp), contentScale = ContentScale.Crop,
                            painter = painterResource(id = R.drawable.image_placeholder),
                            contentDescription = "loading_image")

                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(20.dp)
                                .width(20.dp)
                                .align(Alignment.Center),
                            color = Color.Gray
                        )
                    }
                },
                error = {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp), contentAlignment = Alignment.Center){

                        Image(modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp), contentScale = ContentScale.Crop,
                            painter = painterResource(id = R.drawable.image_placeholder),
                            contentDescription = "error_image")
                    }
                }
            )

        }

        Spacer(modifier = Modifier.height(7.dp))

        if(data.title != ""){
            Text(text = data.title ?: "",
                fontSize = 17.sp,
                fontFamily = customFonts,
                fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(7.dp))
        }

        if(data.description != ""){
            Text(text = data.description ?: "",
                fontSize = 14.sp,
                maxLines = 2,
                fontFamily = customFonts,
                fontWeight = FontWeight.Normal)

            Spacer(modifier = Modifier.height(10.dp))
        }

        if(!isLastIndex){
            Box(modifier = Modifier
                .fillMaxSize()
                .height(0.5.dp)
                .background(color = Color.LightGray))
        }
        
    }
    
}

val customFonts = FontFamily(
    Font(R.font.figtree_light, FontWeight.Light),
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_semibold, FontWeight.Bold)
)

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition(label = "")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500)
        ),
        label = ""
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFEAE9E9),
                Color(0xFFB8B5B5),
                Color(0xFFEAE9E9),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
        .onGloballyPositioned {
            size = it.size
        }
}