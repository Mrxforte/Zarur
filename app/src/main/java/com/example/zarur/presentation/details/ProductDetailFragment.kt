package com.example.zarur.presentation.details

import android.content.Intent
import android.net.Uri
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.zarur.presentation.booking.BookingFlowViewModel
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

import com.example.zarur.util.ensureAuth
import com.example.zarur.util.showAuthRequiredDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@AndroidEntryPoint
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductDetailViewModel by viewModels()
    private val bookingFlowViewModel: BookingFlowViewModel by activityViewModels()
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
                ensureAuth(viewModel.isLoggedIn()) {
                    viewModel.toggleCart()
                }
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
                ensureAuth(viewModel.isLoggedIn()) {
                    selectedRating = (index + 1).toFloat()
                    updateStarInputTints()
                }
            }
        }
        updateStarInputTints()

        binding.etComment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                ensureAuth(viewModel.isLoggedIn()) {
                    binding.llCommentActions.visibility = View.VISIBLE
                }
            }
        }

        binding.etComment.setOnClickListener {
            ensureAuth(viewModel.isLoggedIn()) {
                binding.llCommentActions.visibility = View.VISIBLE
            }
        }

        binding.btnCancelComment.setOnClickListener {
            binding.etComment.text?.clear()
            binding.etComment.clearFocus()
            binding.llCommentActions.visibility = View.GONE
        }

        binding.btnPostComment.setOnClickListener {
            ensureAuth(viewModel.isLoggedIn()) {
                val text = binding.etComment.text?.toString()?.trim() ?: ""
                if (text.isNotEmpty()) {
                    viewModel.submitReview(text, selectedRating)
                } else {
                    showInfoToast(getString(R.string.msg_enter_comment))
                }
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
        binding.tvReviewsHeader.text = getString(R.string.reviews_header_count, reviewsList.size)

        if (reviewsList.isEmpty()) {
            binding.tvEmptyReviews.visibility = View.VISIBLE
            binding.tvEmptyReviews.text = getString(R.string.no_reviews_yet)
            binding.rvReviews.visibility = View.GONE
            binding.btnViewAllReviews.visibility = View.GONE
        } else {
            binding.tvEmptyReviews.visibility = View.GONE
            binding.rvReviews.visibility = View.VISIBLE

            if (reviewsList.size > 2) {
                binding.btnViewAllReviews.visibility = View.VISIBLE
                binding.btnViewAllReviews.text = if (isAllReviewsExpanded) getString(R.string.btn_show_less) else getString(R.string.btn_view_all_count, reviewsList.size)
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
                            binding.ivUserAvatar.setPadding(0,0,0,0)
                            binding.ivUserAvatar.imageTintList = null
                            Glide.with(binding.ivUserAvatar)
                                .load(user.avatarUrl)
                                .into(binding.ivUserAvatar)
                        } else {
                            binding.ivUserAvatar.setPadding(8,8,8,8)
                            binding.ivUserAvatar.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
                            binding.ivUserAvatar.setImageResource(R.drawable.ic_profile)
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
                            is ProductDetailUiEvent.ShowAuthRequiredDialog -> {
                                showAuthRequiredDialog()
                            }
                            is ProductDetailUiEvent.ShowAddToCartSuccess -> {
                                showSuccessToast(getString(R.string.msg_added_to_cart_success))
                            }
                            is ProductDetailUiEvent.ShowRemoveFromCartSuccess -> {
                                showInfoToast(getString(R.string.msg_removed_from_cart))
                            }
                            is ProductDetailUiEvent.NavigateToBooking -> {
                                viewModel.product.value?.let { prod ->
                                    bookingFlowViewModel.initBooking(prod)
                                }
                                if (findNavController().currentDestination?.id == R.id.productDetailFragment) {
                                    findNavController().navigate(R.id.action_productDetailFragment_to_bookRealEstateFragment)
                                }
                            }
                            is ProductDetailUiEvent.NavigateToChat -> {
                                findNavController().navigate(R.id.action_global_chatDetailFragment)
                            }
                            is ProductDetailUiEvent.ShowReviewSuccess -> {
                                showSuccessToast(getString(R.string.msg_review_posted))
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

        if (product.isInCart) {
            binding.btnCart.setImageResource(R.drawable.ic_check_circle)
            binding.btnCart.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green_uzum))
            binding.btnCartTop.setImageResource(R.drawable.ic_check_circle)
            binding.btnCartTop.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green_uzum))
        } else {
            binding.btnCart.setImageResource(R.drawable.ic_add_to_cart)
            binding.btnCart.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary))
            binding.btnCartTop.setImageResource(R.drawable.ic_add_to_cart)
            binding.btnCartTop.setColorFilter(ContextCompat.getColor(requireContext(), R.color.on_surface))
        }
        
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
            val sellerName = tvSellerName?.text?.toString() ?: getString(R.string.official_seller_store)
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

        // Toggling favorite status requires authentication
        binding.fabFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }

        // Initiating checkout/booking requires authentication
        binding.btnAction.setOnClickListener {
            viewModel.buyNow()
        }
        
        // Chatting with seller requires authentication
        binding.btnChat.setOnClickListener {
            viewModel.chat()
        }

        // Toggling cart status requires authentication
        binding.btnCart.setOnClickListener {
            viewModel.toggleCart()
        }

        binding.btnCartTop.setOnClickListener {
            viewModel.toggleCart()
        }

        // Viewing/calling seller phone number requires authentication
        binding.btnCall.setOnClickListener {
            ensureAuth(viewModel.isLoggedIn()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+998901234567"))
                startActivity(intent)
            }
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
