package com.example.zarur.presentation.details

import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.FragmentProductDetailBinding
import com.example.zarur.util.showErrorToast
import com.example.zarur.util.showInfoToast
import com.example.zarur.util.showSuccessToast
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Review
import com.example.zarur.presentation.common.ImageSliderAdapter
import com.example.zarur.presentation.common.ProductAdapter
import com.example.zarur.presentation.common.setupDotsIndicator
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductDetailViewModel by viewModels()
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var relatedProductAdapter: ProductAdapter

    private var isAllReviewsExpanded = false
    private var fullReviewsList: List<Review> = emptyList()
    private var selectedRating: Float = 5.0f
    private lateinit var starInputs: List<ImageView>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductDetailBinding.bind(view)

        val productId = arguments?.getString("productId")
        if (productId != null) {
            viewModel.loadProduct(productId)
        }

        starInputs = listOf(
            binding.starInput1,
            binding.starInput2,
            binding.starInput3,
            binding.starInput4,
            binding.starInput5
        )

        setupRecyclerViews()
        setupInsets()
        setupListeners()
        setupCommentInput()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        reviewAdapter = ReviewAdapter()
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = reviewAdapter

        relatedProductAdapter = ProductAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply { putString("productId", product.id) }
                findNavController().navigate(R.id.productDetailFragment, bundle)
            },
            onAddToCartClick = { _ ->
                viewModel.addToCart()
            }
        )
        binding.rvRelatedProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRelatedProducts.adapter = relatedProductAdapter
    }

    private fun setupInsets() {
        val initialPaddingTop = binding.clToolbarContent.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            binding.clToolbarContent.setPadding(
                binding.clToolbarContent.paddingLeft,
                initialPaddingTop + statusBars.top,
                binding.clToolbarContent.paddingRight,
                binding.clToolbarContent.paddingBottom
            )
            insets
        }
    }

    private fun setupCommentInput() {
        starInputs.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = (index + 1).toFloat()
                updateStarInputTints()
            }
        }
        updateStarInputTints()

        binding.etComment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.llCommentActions.visibility = View.VISIBLE
            }
        }

        binding.etComment.setOnClickListener {
            binding.llCommentActions.visibility = View.VISIBLE
        }

        binding.btnCancelComment.setOnClickListener {
            binding.etComment.text?.clear()
            binding.etComment.clearFocus()
            binding.llCommentActions.visibility = View.GONE
        }

        binding.btnPostComment.setOnClickListener {
            val text = binding.etComment.text?.toString()?.trim() ?: ""
            if (text.isNotEmpty()) {
                viewModel.submitReview(text, selectedRating)
            } else {
                showInfoToast("Please enter a comment")
            }
        }
    }

    private fun updateStarInputTints() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.yellow_uzum)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.neutral_300)

        starInputs.forEachIndexed { index, imageView ->
            if (index < selectedRating.toInt()) {
                imageView.imageTintList = ColorStateList.valueOf(activeColor)
            } else {
                imageView.imageTintList = ColorStateList.valueOf(inactiveColor)
            }
        }
    }

    private fun updateReviewsList(reviewsList: List<Review>) {
        fullReviewsList = reviewsList
        binding.tvReviewsHeader.text = "Reviews (${reviewsList.size})"

        if (reviewsList.isEmpty()) {
            binding.tvEmptyReviews.visibility = View.VISIBLE
            binding.tvEmptyReviews.text = "No reviews yet. Be the first to leave a review!"
            binding.rvReviews.visibility = View.GONE
            binding.btnViewAllReviews.visibility = View.GONE
        } else {
            binding.tvEmptyReviews.visibility = View.GONE
            binding.rvReviews.visibility = View.VISIBLE

            if (reviewsList.size > 2) {
                binding.btnViewAllReviews.visibility = View.VISIBLE
                binding.btnViewAllReviews.text = if (isAllReviewsExpanded) "Show less" else "View all (${reviewsList.size})"
                reviewAdapter.submitList(if (isAllReviewsExpanded) reviewsList else reviewsList.take(2))
            } else {
                binding.btnViewAllReviews.visibility = View.GONE
                reviewAdapter.submitList(reviewsList)
            }
        }
    }

    private fun observeViewModel() {
        binding.shimmerProductDetail.root.isVisible = true
        binding.shimmerProductDetail.shimmerDetailContainer.startShimmer()
        var isFirstEmit = true
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.product.collect { product ->
                        if (product != null) {
                            bindProduct(product)
                            if (isFirstEmit) {
                                isFirstEmit = false
                                delay(1000)
                            }
                            binding.shimmerProductDetail.shimmerDetailContainer.stopShimmer()
                            binding.shimmerProductDetail.root.isVisible = false
                        }
                    }
                }
                launch {
                    viewModel.reviews.collect { reviewsList ->
                        updateReviewsList(reviewsList)
                    }
                }
                launch {
                    viewModel.userProfile.collect { user ->
                        if (user != null && !user.avatarUrl.isNullOrEmpty()) {
                            Glide.with(binding.ivUserAvatar)
                                .load(user.avatarUrl)
                                .placeholder(R.drawable.onboarding1)
                                .into(binding.ivUserAvatar)
                        } else {
                            binding.ivUserAvatar.setImageResource(R.drawable.onboarding1)
                        }
                    }
                }
                launch {
                    viewModel.relatedProducts.collect { products ->
                        relatedProductAdapter.submitList(products)
                    }
                }
                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is ProductDetailUiEvent.NavigateToAuth -> {
                                findNavController().navigate(R.id.authHubFragment)
                            }
                            is ProductDetailUiEvent.ShowAddToCartSuccess -> {
                                showSuccessToast("Added to cart successfully")
                            }
                            is ProductDetailUiEvent.NavigateToBooking -> {
                                if (findNavController().currentDestination?.id == R.id.productDetailFragment) {
                                    findNavController().navigate(R.id.action_productDetailFragment_to_bookRealEstateFragment)
                                }
                            }
                            is ProductDetailUiEvent.NavigateToChat -> {
                                findNavController().navigate(R.id.chatDetailFragment)
                            }
                            is ProductDetailUiEvent.ShowReviewSuccess -> {
                                showSuccessToast("Review posted!")
                                binding.etComment.text?.clear()
                                binding.etComment.clearFocus()
                                binding.llCommentActions.visibility = View.GONE
                            }
                            is ProductDetailUiEvent.ShowReviewError -> {
                                showErrorToast(event.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindProduct(product: Product) {
        val images = product.getAllImages()
        binding.vpHeroSlider.adapter = ImageSliderAdapter(images)

        if (images.size > 1) {
            binding.llHeroDotsIndicator.visibility = View.VISIBLE
            setupDotsIndicator(binding.llHeroDotsIndicator, images.size, 0)

            binding.vpHeroSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setupDotsIndicator(binding.llHeroDotsIndicator, images.size, position)
                }
            })
        } else {
            binding.llHeroDotsIndicator.visibility = View.GONE
        }
            
        binding.fabFavorite.setImageResource(R.drawable.ic_favorites)
        binding.fabFavorite.setColorFilter(
            if (product.isFavorite) {
                requireContext().getColor(R.color.red_uzum)
            } else {
                requireContext().getColor(R.color.neutral_400)
            }
        )

        binding.tvProductName.text = product.name
        
        binding.tvPrice.text = "%,.0f uzs".format(product.price).replace(",", " ")
        binding.tvLocation.text = product.location
        val countText = if (product.reviewCount > 0) " (${product.reviewCount})" else ""
        binding.tvRating.text = "%.1f%s".format(product.rating, countText)
        
        if (product.discount > 0) {
            binding.tvDiscount.visibility = View.VISIBLE
            binding.tvDiscount.text = "-${product.discount}%"

            val originalPrice = (product.price * 100.0) / (100 - product.discount)
            binding.tvOriginalPrice.visibility = View.VISIBLE
            binding.tvOriginalPrice.text = "%,.0f uzs".format(originalPrice).replace(",", " ")
            binding.tvOriginalPrice.paintFlags = binding.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.tvDiscount.visibility = View.GONE
            binding.tvOriginalPrice.visibility = View.GONE
        }

        // Dynamic categories layout
        binding.flDynamicContent.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        
        val layoutId = when (product.category) {
            "Cars" -> R.layout.layout_details_auto
            "Real Estate" -> R.layout.layout_details_realty
            else -> R.layout.layout_details_generic
        }
        val detailsView = inflater.inflate(layoutId, binding.flDynamicContent, false)
        binding.flDynamicContent.addView(detailsView)

        // Setup Visit Seller button
        val btnVisitSeller = detailsView.findViewById<View>(R.id.btnVisitSeller)
        val tvSellerName = detailsView.findViewById<TextView>(R.id.tvSellerName)
        btnVisitSeller?.setOnClickListener {
            val sellerName = tvSellerName?.text?.toString() ?: "Official Seller Store"
            val bundle = Bundle().apply {
                putString("sellerId", product.sellerId.ifEmpty { "seller1" })
                putString("sellerName", sellerName)
            }
            findNavController().navigate(R.id.action_productDetailFragment_to_sellerStoreFragment, bundle)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.fabFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }

        binding.btnAction.setOnClickListener {
            viewModel.buyNow()
        }
        
        binding.btnChat.setOnClickListener {
            viewModel.chat()
        }

        binding.btnViewAllReviews.setOnClickListener {
            isAllReviewsExpanded = !isAllReviewsExpanded
            updateReviewsList(fullReviewsList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
