package com.example.zarur.presentation.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.R
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpCenterFragment : Fragment(R.layout.fragment_help_center) {

    private lateinit var faqAdapter: FaqAdapter
    private var allFaqs: List<FaqItem> = emptyList()
    private var selectedCategory: String = "All"
    private var searchQuery: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.toolbar).setOnClickListener {
            findNavController().navigateUp()
        }

        setupFaqData()
        setupRecyclerView(view)
        setupSearchAndFilters(view)
        setupTabsAndContact(view)
    }

    private fun setupFaqData() {
        allFaqs = listOf(
            FaqItem(
                category = "General",
                question = getString(R.string.label_faq_what_is_zarur),
                answer = "Zarur — bu ko'chmas mulk (uylar, kvartiralar, villalar), avtomobillar hamda turli xil e'lonlar va mahsulotlarni sotish, xarid qilish va band qilish (booking) imkonini beruvchi zamonaviy marketplace ilovasidir."
            ),
            FaqItem(
                category = "Selling",
                question = getString(R.string.label_faq_how_to_post),
                answer = "Profil bo'limidagi 'Sotuvchi rejimiga o'tish' (Switch to Seller Mode) tugmasini bosing. Sotuvchi panelida 'E'lon yaratish' tugmasi orqali rasm yuklab, sarlavha, narx va toifa ma'lumotlarini kiritib e'loningizni chop etishingiz mumkin."
            ),
            FaqItem(
                category = "Buying",
                question = "Mulk yoki mahsulotni qanday band qilish (Book) mumkin?",
                answer = "Yoqtirgan e'loningiz sahifasiga kirib, 'Hozir band qilish' (Book Now) tugmasini bosing. Kerakli sana va to'lov turini tanlab, to'lovni tasdiqlang. Muvaffaqiyatli band qilingandan so'ng elektron kvitansiya (E-Receipt) shakllanadi."
            ),
            FaqItem(
                category = "Buying",
                question = "Sotuvchi bilan qanday bog'lanish mumkin?",
                answer = "Har bir e'lon sahifasida 'Chat' hamda qo'ng'iroq (Ovozli va Video) tugmalari mavjud. Siz ilova ichida sotuvchiga xabar yozishingiz va to'g'ridan-to me'zonida muloqot qilishingiz mumkin."
            ),
            FaqItem(
                category = "Buying",
                question = "Savat va Saralanganlar bo'limi qanday ishlaydi?",
                answer = "E'lon kartasidagi yurakcha belgisini bosish orqali saralanganlarga, savat tugmasi orqali esa xarid savatiga qo'shishingiz mumkin. Pastki menyudagi 'Favorites' bo'limi orqali barcha saqlangan mahsulotlarni boshqarishingiz mumkin."
            ),
            FaqItem(
                category = "General",
                question = getString(R.string.label_faq_is_it_safe),
                answer = "Barcha foydalanuvchilar va to'lov tranzaksiyalari Firebase xavfsizlik protokollari orqali himoyalanadi. Siz uzcard, humo, visa yoki boshqa to'lov tizimlaridan xavfsiz foydalanishingiz mumkin."
            ),
            FaqItem(
                category = "Account",
                question = "Profil va ilova tilini qanday o'zgartirish mumkin?",
                answer = "Profil -> 'Til' (Language) bo'limiga kirib O'zbekcha, Ruscha yoki Inglizcha tillaridan birini tanlashingiz mumkin. Shuningdek, Profil -> 'Tungi rejim' orqali quyuq va yorug' mavzularni almashtirishingiz mumkin."
            ),
            FaqItem(
                category = "Account",
                question = "Parolni unutgan bo'lsam, qanday tiklayman?",
                answer = "Kirish sahifasidagi 'Parolni unutdingizmi?' havolasini bosing. Emailingizga tiklash havolasi yuboriladi va u orqali yangi parol o'rnatishingiz mumkin."
            )
        )
    }

    private fun setupRecyclerView(view: View) {
        val rvFaqList = view.findViewById<RecyclerView>(R.id.rvFaqList)
        rvFaqList.layoutManager = LinearLayoutManager(context)
        faqAdapter = FaqAdapter(allFaqs)
        rvFaqList.adapter = faqAdapter
    }

    private fun setupSearchAndFilters(view: View) {
        val etHelpSearch = view.findViewById<EditText>(R.id.etHelpSearch)
        etHelpSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterFaqs()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupCategories)
        chipGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
            selectedCategory = when (checkedIds.firstOrNull()) {
                R.id.chipGeneral -> "General"
                R.id.chipBuying -> "Buying"
                R.id.chipSelling -> "Selling"
                R.id.chipAccount -> "Account"
                else -> "All"
            }
            filterFaqs()
        }
    }

    private fun filterFaqs() {
        val filtered = allFaqs.filter { item ->
            val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isEmpty() ||
                    item.question.contains(searchQuery, ignoreCase = true) ||
                    item.answer.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        faqAdapter.updateList(filtered)
    }

    private fun setupTabsAndContact(view: View) {
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val svCategories = view.findViewById<View>(R.id.svCategories)
        val rvFaqList = view.findViewById<View>(R.id.rvFaqList)
        val llContactSupport = view.findViewById<View>(R.id.llContactSupport)

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    svCategories.visibility = View.VISIBLE
                    rvFaqList.visibility = View.VISIBLE
                    llContactSupport.visibility = View.GONE
                } else {
                    svCategories.visibility = View.GONE
                    rvFaqList.visibility = View.GONE
                    llContactSupport.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        view.findViewById<View>(R.id.btnCustomerCall)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+998712000000"))
            startActivity(intent)
        }

        view.findViewById<View>(R.id.btnTelegramSupport)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/zarursupport"))
            startActivity(intent)
        }
    }

    private data class FaqItem(
        val category: String,
        val question: String,
        val answer: String,
        var isExpanded: Boolean = false
    )

    private inner class FaqAdapter(private var items: List<FaqItem>) :
        RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

        fun updateList(newList: List<FaqItem>) {
            items = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
            return FaqViewHolder(v)
        }

        override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class FaqViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvQuestion = view.findViewById<TextView>(R.id.tvQuestion)
            private val tvAnswer = view.findViewById<TextView>(R.id.tvAnswer)
            private val ivArrow = view.findViewById<ImageView>(R.id.ivArrow)

            fun bind(item: FaqItem) {
                tvQuestion.text = item.question
                tvAnswer.text = item.answer

                tvAnswer.isVisible = item.isExpanded
                ivArrow.rotation = if (item.isExpanded) 90f else 0f

                itemView.setOnClickListener {
                    item.isExpanded = !item.isExpanded
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }
}
