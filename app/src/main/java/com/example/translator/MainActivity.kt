package com.example.translator

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.listview_item.*
import kotlinx.android.synthetic.main.listview_item.view.*
import okhttp3.*
import org.jetbrains.anko.doAsync
import java.io.IOException

const val URI = "https://synmtranslate.herokuapp.com/api_server/result"

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        go.setOnClickListener {
            if(textToTranslate.text.isBlank()){
                runOnUiThread {
                    Toast.makeText(this,"Please type something in the text field",Toast.LENGTH_LONG).show()
                }
            }else{
//                closing keyboard
                if (currentFocus != null) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                }
                Toast.makeText(this,"Translating...",Toast.LENGTH_LONG).show()
//                some async stf going on here
                doAsync{
                    translate(textToTranslate.text.toString())
                }
//                clearing text field
                textToTranslate.setText("")
            }
        }
//        if(translateHistory.size != 0){
//        }
        clearButton.setOnClickListener{
            Toast.makeText(this,"Cleared", Toast.LENGTH_SHORT).show()
            translateHistory.clear()
            clearButton.isVisible = false
            recyclerView.removeAllViews()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
    }


//    Make api call and translate
    private fun translate(word:String){
        val okHttpClient = OkHttpClient()
        val requestBody = FormBody.Builder().add("yor_text",word).build()
        val request = Request.Builder()
            .method("POST", requestBody)
            .url(URI)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback{
//            If my request fails or something happens
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity,e.message.toString(),Toast.LENGTH_LONG).show()
                }
            }

//            If request is a success
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gson = GsonBuilder().create().fromJson(body,TranslateObject::class.java)
                runOnUiThread {
                    if(gson.result.isEmpty()){
                        Toast.makeText(this@MainActivity,"Word not found",Toast.LENGTH_LONG).show()
                    }else{
                        clearButton.isVisible = true
                        translateHistory.add(0, Helper(gson,word))
                        recyclerView.adapter = AppAdaptor()
                        AppAdaptor().notifyItemInserted(0)
                    }
                }
            }

        }
        )
    }
}

//App Models
data class TranslateObject(val result:List<String>)
data class Helper(val translated:TranslateObject,val yorubaWord:String)
var translateHistory:MutableList<Helper> = mutableListOf()


//Adaptor and ViewHolder
internal class AppAdaptor : RecyclerView.Adapter<MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.listview_item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.yorubaWord.text = translateHistory[position].yorubaWord
        when (
            translateHistory[position].translated.result.count()
        ){
            1 -> {
                holder.itemView.englishWord.text =translateHistory[position].translated.result[0]
                holder.itemView.englishWord1.isVisible = false
                holder.itemView.englishWord2.isVisible = false
            }
            2->{
                holder.itemView.englishWord1.text = translateHistory[position].translated.result[1]
                holder.itemView.englishWord.text = translateHistory[position].translated.result[0]
                holder.itemView.englishWord2.isVisible = false
            }
            3->{
                holder.itemView.englishWord1.text = translateHistory[position].translated.result[0]
                holder.itemView.englishWord.text = translateHistory[position].translated.result[1]
                holder.itemView.englishWord2.text = translateHistory[position].translated.result[2]
            }
        }
    }
    override fun getItemCount(): Int {
        return translateHistory.size
    }
}

internal class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


