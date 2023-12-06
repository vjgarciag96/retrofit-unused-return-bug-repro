package com.vicgarci.repro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiService = ExampleApiService.create()

        setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                Test(index = 1) {
                    lifecycleScope.launch {
                        apiService.postOperation1()
                    }
                }
                Test(index = 2) {
                    lifecycleScope.launch {
                        val result = apiService.postOperation2()

                        // Do something with the response body so R8 does not get rid of it
                        if (result.isSuccessful) {
                            Toast.makeText(
                                this@MainActivity,
                                "Success! ${result.body()?.status}",
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Error :(",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Test(
    index: Int,
    onClick: () -> Unit,
) {
    Button(onClick = onClick) {
        Text(text = "Trigger API call $index")
    }
}
