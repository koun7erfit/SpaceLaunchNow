package me.calebjones.spacelaunchnow.common.ui.supporter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatButton;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.google.common.collect.ImmutableList;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.transitionseverywhere.TransitionManager;


import java.util.ArrayList;
import java.util.List;


import me.calebjones.spacelaunchnow.common.R;
import me.calebjones.spacelaunchnow.common.base.BaseActivity;
import me.calebjones.spacelaunchnow.common.databinding.ActivitySupportBinding;
import timber.log.Timber;

import static me.calebjones.spacelaunchnow.common.ui.supporter.SupporterHelper.isOwned;

public class SupporterActivity extends BaseActivity {


    private boolean isAvailable = false;
    private boolean isRefreshable = true;
    private Dialog dialog;
    private List<String> ownedProducts;
    private ActivitySupportBinding binding;


    private PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    };

    private void handlePurchase(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
            runOnUiThread(() -> {
                // Handle the success of the consume operation.
                List<String> purchases = purchase.getProducts();
                for (String purchase_sku : purchases) {
                    Timber.v(purchase_sku);
                    animatePurchase();
                    SupporterHelper.saveProductAsOwned(purchase_sku);
                }
            });
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    private BillingClient billingClient;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        context = this;

        ownedProducts = new ArrayList<>();

        if (SupporterHelper.is2024Supporter()) {
            binding.purchase.setText(R.string.support_us_2022);
            binding.detailTitle.setText("Thank You!");
            binding.detailSubTitle.setText("Here's to another year...");
        } else if (SupporterHelper.isSupporter() && !SupporterHelper.is2024Supporter()) {
            binding.purchase.setText(R.string.support_again_2024);
            binding.detailTitle.setText("Become a 2024 Supporter");
            binding.detailSubTitle.setText("Continue Your Support!");
        } else {
            binding.purchase.setText(R.string.supporter_title_2022);
            binding.detailTitle.setText("Become a Supporter");
            binding.detailSubTitle.setText("Get Pro Features");
        }
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setTitle("");

        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Timber.v("READY!");
                    restorePurchases();
                    isAvailable = true;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        binding.purchase.setOnClickListener(v -> checkClick());
        binding.fabSupporter.setOnClickListener(v -> checkClick());
    }

    private void restorePurchases() {
        Timber.v("onRestorePurchases running...");
        billingClient.queryPurchaseHistoryAsync(
                QueryPurchaseHistoryParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, purchasesHistoryList) -> {
                    // check billingResult
                    // process returned purchase history list, e.g. display purchase history
                    Timber.v(billingResult.getDebugMessage());
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (purchasesHistoryList != null && !purchasesHistoryList.isEmpty()) {
                            for (PurchaseHistoryRecord purchase : purchasesHistoryList) {
                                handlePurchaseHistoryRecord(purchase);
                            }
                        }
                    }
                }
        );
    }

    private void handlePurchaseHistoryRecord(PurchaseHistoryRecord purchaseHistoryRecord) {
        runOnUiThread(() -> {
            List<String> purchases = purchaseHistoryRecord.getProducts();
            for (String purchase : purchases) {
                Timber.v(purchase);
                SupporterHelper.saveProductAsOwnedAsync(purchase);
                animatePurchase();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        if (id == R.id.action_restore) {
            isRefreshable = true;
        }

        if (id == R.id.action_support) {
            new MaterialDialog.Builder(this)
                    .title(R.string.need_support)
                    .content(R.string.need_support_description)
                    .neutralText(R.string.email)
                    .positiveColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .neutralColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .negativeText(R.string.cancel)
                    .positiveText(R.string.discord)
                    .onNeutral((dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@spacelaunchnow.me"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Space Launch Now - Feedback");

                        startActivity(Intent.createChooser(intent, "Email via..."));
                    })
                    .onPositive((dialog, which) -> {
                        String url = "https://discord.gg/WVfzEDW";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    })
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_support, menu);
        return true;
    }

    public void checkClick() {
        if (billingClient.isReady()) {
            QueryProductDetailsParams queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(
                                    ImmutableList.of(
                                            QueryProductDetailsParams.Product.newBuilder()
                                                    .setProductId(SupporterHelper.SKU_2024_BRONZE)
                                                    .setProductType(BillingClient.ProductType.INAPP)
                                                    .build(),
                                            QueryProductDetailsParams.Product.newBuilder()
                                                    .setProductId(SupporterHelper.SKU_2024_METAL)
                                                    .setProductType(BillingClient.ProductType.INAPP)
                                                    .build(),
                                            QueryProductDetailsParams.Product.newBuilder()
                                                    .setProductId(SupporterHelper.SKU_2024_SILVER)
                                                    .setProductType(BillingClient.ProductType.INAPP)
                                                    .build(),
                                            QueryProductDetailsParams.Product.newBuilder()
                                                    .setProductId(SupporterHelper.SKU_2024_PLATINUM)
                                                    .setProductType(BillingClient.ProductType.INAPP)
                                                    .build()
                                    ))
                            .build();


            billingClient.queryProductDetailsAsync(
                    queryProductDetailsParams,
                    (billingResult, productDetailsList) -> {
                        // check billingResult
                        // process returned productDetailsList

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            showPurchaseDialog(productDetailsList);
                        } else {
                            Timber.v(billingResult.getDebugMessage());
                        }
                    }
            );
        }
    }

    private void showPurchaseDialog(List<ProductDetails> productDetailsList) {
        runOnUiThread(() -> {


            View view = getLayoutInflater().inflate(R.layout.supporter_dialog, null);
            dialog = new BottomSheetDialog(context);
            dialog.setContentView(view);

            View bronzeView = view.findViewById(R.id.bronze_group);
            AppCompatButton bronzeButton = view.findViewById(R.id.bronze_button);
            TextView bronzeTitle = view.findViewById(R.id.bronze_title);
            TextView bronzeDescription = view.findViewById(R.id.bronze_description);

            View metalView = view.findViewById(R.id.metal_group);
            AppCompatButton metalButton = view.findViewById(R.id.metal_button);
            TextView metalTitle = view.findViewById(R.id.metal_title);
            TextView metalDescription = view.findViewById(R.id.metal_description);

            View silverView = view.findViewById(R.id.silver_group);
            AppCompatButton silverButton = view.findViewById(R.id.silver_button);
            TextView silverTitle = view.findViewById(R.id.silver_title);
            TextView silverDescription = view.findViewById(R.id.silver_description);


            View platinumView = view.findViewById(R.id.platinum_group);
            AppCompatButton platinumButton = view.findViewById(R.id.platinum_button);
            TextView platinumTitle = view.findViewById(R.id.platinum_title);
            TextView platinumDescription = view.findViewById(R.id.platinum_description);

            TextView bottomMessage = view.findViewById(R.id.bottom_message);
            TextView description = view.findViewById(R.id.descriptionView);

            if (SupporterHelper.is2024Supporter()) {
                description.setText(R.string.thanks_support_development);
            } else if (SupporterHelper.isSupporter() && !SupporterHelper.is2024Supporter()) {
                description.setText(R.string.supporter_unlock_text_previous);
            } else {
                description.setText(R.string.supporter_unlock_text);
            }

            if (isAvailable) {
                bronzeView.setVisibility(View.VISIBLE);
                metalView.setVisibility(View.VISIBLE);
                silverView.setVisibility(View.VISIBLE);
                platinumView.setVisibility(View.VISIBLE);

                bottomMessage.setText("Pay what you want - get the same features!");
            } else {
                bronzeView.setVisibility(View.GONE);
                metalView.setVisibility(View.GONE);
                silverView.setVisibility(View.GONE);
                platinumView.setVisibility(View.GONE);

                bottomMessage.setText("Unable to load in-app-products.");
            }


            for (ProductDetails productDetail : productDetailsList) {
                Timber.v("Checking %s...", productDetail.getName());
                switch (productDetail.getProductId()) {
                    case SupporterHelper.SKU_2024_BRONZE:
                        configureProductItem(bronzeButton, bronzeTitle, bronzeDescription, productDetail,
                                SupporterHelper.SKU_2024_BRONZE,
                                new IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_thumbs_up)
                                        .color(Color.WHITE)
                                        .sizeDp(24));
                        break;

                    case SupporterHelper.SKU_2024_METAL:
                        configureProductItem(metalButton, metalTitle, metalDescription, productDetail,
                                SupporterHelper.SKU_2024_METAL,
                                new IconicsDrawable(this)
                                        .icon(GoogleMaterial.Icon.gmd_local_cafe)
                                        .color(Color.WHITE)
                                        .sizeDp(24));
                        break;

                    case SupporterHelper.SKU_2024_SILVER:
                        configureProductItem(silverButton, silverTitle, silverDescription, productDetail,
                                SupporterHelper.SKU_2024_SILVER,
                                new IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_store)
                                        .color(Color.WHITE)
                                        .sizeDp(24));
                        break;

                    case SupporterHelper.SKU_2024_PLATINUM:
                        configureProductItem(platinumButton, platinumTitle, platinumDescription, productDetail,
                                SupporterHelper.SKU_2024_PLATINUM,
                                new IconicsDrawable(this)
                                        .icon(FontAwesome.Icon.faw_money_bill_wave)
                                        .color(Color.WHITE)
                                        .sizeDp(24));
                        break;
                }
            }
            dialog.show();
        });
    }

    private void configureProductItem(AppCompatButton button, TextView titleView,
                                      TextView descriptionView,
                                      ProductDetails productDetail, String sku,
                                      IconicsDrawable drawable) {
        String price = "N/A";
        String title = "Product";
        String description = "Unable to get product description.";

        if (productDetail.getOneTimePurchaseOfferDetails() != null) {
            price = productDetail.getOneTimePurchaseOfferDetails().getFormattedPrice();
        }
        title = productDetail.getTitle();
        description = productDetail.getDescription();

        if (isOwned(productDetail.getProductId())) {
            button.setCompoundDrawablesRelative(
                    new IconicsDrawable(this)
                            .icon(FontAwesome.Icon.faw_check)
                            .color(Color.WHITE)
                            .sizeDp(24),
                    null, null, null);
            price = "purchased";
        } else {
            button.setCompoundDrawablesRelative(drawable, null, null, null);
        }

        button.setText(price);
        titleView.setText(title);
        descriptionView.setText(description);

        button.setOnClickListener(view1 -> {
            dialog.dismiss();
            makePurchase(productDetail);
        });

    }


    private void makePurchase(ProductDetails productDetail) {

        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                .setProductDetails(productDetail)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        // Launch the billing flow
        BillingResult billingResult = billingClient.launchBillingFlow(this, billingFlowParams);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void animatePurchase() {
        runOnUiThread(() -> {
            if (binding.supportThankYou.getVisibility() != View.VISIBLE) {
                enterReveal(binding.supportThankYou);
                if (SupporterHelper.is2024Supporter()) {
                    binding.purchase.setText(R.string.support_us_2022);
                    binding.detailTitle.setText("Thank You!");
                    binding.detailSubTitle.setText("Here's to another year...");
                } else if (SupporterHelper.isSupporter() && !SupporterHelper.is2024Supporter()) {
                    binding.purchase.setText(R.string.support_again_2024);
                    binding.detailTitle.setText("Become a 2024 Supporter");
                    binding.detailSubTitle.setText("Continue Your Support!");
                } else {
                    binding.purchase.setText(R.string.supporter_title_2022);
                    binding.detailTitle.setText("Become a Supporter");
                    binding.detailSubTitle.setText("Get Pro Features");
                }
            }
        });
    }

    void enterReveal(View view) {
        runOnUiThread(() -> {
            // previously invisible view
            final View myView = view;
            //To not have empty scroll, the container is INVISIBLE with 0dp height.
            //Otherwise the Reveal effect will not work at the first click.
            //Here I set the parameters programmatically.
            myView.setLayoutParams(new AppBarLayout.LayoutParams(
                    AppBarLayout.LayoutParams.MATCH_PARENT,
                    AppBarLayout.LayoutParams.WRAP_CONTENT
            ));
            TransitionManager.beginDelayedTransition(binding.coordinatorLayout);
            myView.setVisibility(View.VISIBLE);
        });
    }
}
