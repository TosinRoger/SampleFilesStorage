package br.com.tosin.filesstorageexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.com.tosin.filesstorageexample.ui.main.MainFragment
import br.com.tosin.filesstorageexample.ui.premain.PreMainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PreMainFragment.newInstance())
                .commitNow()
        }
    }
}