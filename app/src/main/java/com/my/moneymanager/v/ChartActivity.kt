package com.my.moneymanager.v

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.my.moneymanager.R
import com.my.moneymanager.databinding.ActivityChartBinding
import com.my.moneymanager.m.bean.Record
import com.my.moneymanager.m.bean.Records
import com.my.moneymanager.vm.ChartVM

class ChartActivity : AppCompatActivity() {
    private lateinit var mViewModel: ChartVM
    private lateinit var mBinding: ActivityChartBinding
    private lateinit var list: ArrayList<Record>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chart)
        mViewModel = ViewModelProvider(this).get(ChartVM::class.java)
        mBinding.vm = mViewModel//这句没加的话，xml里面设置的东西就不会生效，比如点击事件
        mBinding.lifecycleOwner = this//重要！DataBinding加上这句之后，绑定了LiveData数据源的xml控件才会随着数据变化而改变。
        val records: Records? = intent.getParcelableExtra("DATA")
        records?.let {
            list = it.list as ArrayList<Record>
            if (list.isEmpty()) {
                finish()
                return
            }
            mBinding.cv.setData(list, it.totalStart)
        }
    }
}