package com.my.moneymanager.v

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import com.my.moneymanager.R
import com.my.moneymanager.databinding.ActivityMainBinding
import com.my.moneymanager.m.MyRepository
import com.my.moneymanager.m.bean.Record
import com.my.moneymanager.m.bean.TypeData
import com.my.moneymanager.v.binder.RecordBinder
import com.my.moneymanager.vm.MainVM
import com.my.moneymanager.xutil.MyUtil
import com.my.moneymanager.xutil.TypeDataNames
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 主页面
 */
class MainActivity : AppCompatActivity() {
    private lateinit var mViewModel: MainVM
    private val mList = ArrayList<Record>()
    private lateinit var mAdapter: MultiTypeAdapter
    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        initDataFromLocal()
        observeData()
    }

    /**
     * 初始化
     */
    private fun init() {
        EventBus.getDefault().register(this)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mViewModel = ViewModelProvider(this).get(MainVM::class.java)
        mBinding.vm = mViewModel//这句没加的话，xml里面设置的东西就不会生效，比如点击事件
        mBinding.lifecycleOwner = this//重要！DataBinding加上这句之后，绑定了LiveData数据源的xml控件才会随着数据变化而改变。
        mAdapter = MultiTypeAdapter(mList)
        mAdapter.register(
            Record::class.java,
            RecordBinder()
        )
        mBinding.rv.apply {
            layoutManager = GridLayoutManager(context, 1)
            setHasFixedSize(true)
            adapter = mAdapter
        }
        MyUtil.setHelper(mBinding.rv, mList, mAdapter)
    }

    private fun initDataFromLocal() {
        val repository = MyRepository()
        if (repository.recordList == null || (repository.recordList as ArrayList<Record>).isEmpty()) {
            AlertDialog.Builder(this).apply {
                val inputStream = resources.openRawResource(R.raw.records)
                try {
                    val reader = InputStreamReader(inputStream, "utf-8")
                    val bufferedReader = BufferedReader(reader)
                    var line: String
                    while (true) {
                        line = bufferedReader.readLine()
                        if (line == null) break
                        line = line.replace("(\\t)+".toRegex(), "")//将空格过滤掉
                        repository.add(
                            Record(
                                0,
                                MyUtil.getFormatCreateTime(),
                                line.substring(0, 8),
                                "",
                                line.substring(8)
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                setTitle("已从文档中重载基础数据。")
                setPositiveButton("知道了", null)
                create()
                show()
            }
        }
    }

    /**
     * 观察数据变化
     */
    private fun observeData() {
        mViewModel.apply {
            val owner: LifecycleOwner = this@MainActivity
            dataList.observe(owner, Observer {
                if (mList.size > 0) {
                    mList.clear()
                    mAdapter.items = mList
                    mAdapter.notifyDataSetChanged()
                }
                mList.addAll(it)
                mAdapter.items = mList
                mAdapter.notifyDataSetChanged()
            })
            searchFlag.observe(owner, Observer {
                if (it) {
                    val str: String = mBinding.et.text.toString().trim()
                    mBinding.et.setText(str)
                    mBinding.et.setSelection(str.length)
                } else {
                    refreshData()
                }
            })
            refreshData()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun threadHandle(typeData: TypeData) {
        when (typeData.name) {
            TypeDataNames.clickRecord -> {
                val record = typeData.data as Record
                AlertDialog.Builder(this).apply {
                    setTitle("操作")
                    setNeutralButton("删除") { dialog, _ ->
                        mViewModel.deleteRecord(record.createTime)
                        dialog.dismiss()
                    }
                    setNegativeButton("更新") { dialog, _ ->
                        mViewModel.tryUpdateRecord(record)
                        val str: String = record.date + record.desc + record.money
                        mBinding.et.setText(str)
                        mBinding.et.setSelection(str.length)
                        dialog.dismiss()
                    }
                    setPositiveButton("取消", null)
                    create()
                    show()
                }
            }
        }
    }
}