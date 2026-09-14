package com.example.unipathsa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UniversitySearchActivity : AppCompatActivity() {

    private lateinit var rvUniversities: RecyclerView
    private lateinit var adapter: UniversityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_university_search)

        rvUniversities = findViewById(R.id.rvUniversities)
        rvUniversities.layoutManager = LinearLayoutManager(this)

        val uniList = getMockUniversities()
        adapter = UniversityAdapter(uniList) { university ->
            val intent = Intent(this, UniversityDetailsActivity::class.java)
            intent.putExtra("university", university)
            startActivity(intent)
        }
        rvUniversities.adapter = adapter
    }

    private fun getMockUniversities(): List<University> {
        return listOf(
            University(
                "University of Cape Town",
                "Cape Town, Western Cape",
                "UCT is a public research university located in Cape Town.",
                "The University of Cape Town (UCT) is a public research university located in Cape Town in the Western Cape province of South Africa. UCT was founded in 1829 as the South African College, making it the oldest higher education institute in South Africa.",
                "Engineering, Commerce, Humanities, Science, Health Sciences, Law",
                "NSC with Bachelor pass, specific subject requirements per faculty.",
                "Applications open in April and close in September.",
                "Ranked as the top university in Africa.",
                android.R.drawable.ic_menu_gallery
            ),
            University(
                "University of the Witwatersrand",
                "Johannesburg, Gauteng",
                "Wits is a multi-campus South African public research university.",
                "The University of the Witwatersrand, Johannesburg, is a multi-campus South African public research university situated in the northern areas of central Johannesburg.",
                "Architecture, Business, Medicine, Engineering, Humanities",
                "High APS scores required, English HL/FAL, Mathematics.",
                "Apply online through the Wits student portal.",
                "Known for its strong research output and urban campus.",
                android.R.drawable.ic_menu_gallery
            ),
            University(
                "University of Pretoria",
                "Pretoria, Gauteng",
                "UP is one of South Africa's largest and most influential universities.",
                "The University of Pretoria is a multi-campus public research university in Pretoria, the administrative and de facto capital of South Africa.",
                "Veterinary Science, Agriculture, Law, Theology, Economic and Management Sciences",
                "NSC Bachelor pass, minimum APS varies by programme.",
                "Apply before 30 September for most courses.",
                "Features the only Faculty of Veterinary Science in South Africa.",
                android.R.drawable.ic_menu_gallery
            ),
            University(
                "Stellenbosch University",
                "Stellenbosch, Western Cape",
                "A leading research-intensive university in South Africa.",
                "Stellenbosch University is a public research university situated in Stellenbosch, a town in the Western Cape province of South Africa.",
                "AgriSciences, Arts and Social Sciences, Education, Medicine, Military Science",
                "Strong academic record, NBTs often required.",
                "Online applications via the university website.",
                "Located in the heart of the Winelands.",
                android.R.drawable.ic_menu_gallery
            )
        )
    }
}
