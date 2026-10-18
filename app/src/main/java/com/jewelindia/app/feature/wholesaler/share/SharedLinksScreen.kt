package com.jewelindia.app.feature.wholesaler.share

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.manrope
import com.jewelindia.app.design.sfPro

@Composable
fun SharedLinksScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogueShareViewModel = remember { CatalogueShareViewModel() }
) {
    val context = LocalContext.current
    val shares by viewModel.shares.collectAsState()
    val isLoadingShares by viewModel.isLoadingShares.collectAsState()
    val sharesLoadError by viewModel.sharesLoadError.collectAsState()
    val revokingShareId by viewModel.revokingShareId.collectAsState()
    val actionNotice by viewModel.actionNotice.collectAsState()

    LaunchedEffect(actionNotice) {
        actionNotice?.let { notice ->
            Toast.makeText(context, notice, Toast.LENGTH_SHORT).show()
            viewModel.clearActionNotice()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Palette.dark
                )
            }

            Text(
                text = Copy.CatalogueShare.sharedLinksMenu,
                style = sfPro(20f, JewelWeight.Bold),
                color = Palette.dark
            )

            IconButton(
                onClick = { viewModel.loadShares() },
                enabled = !isLoadingShares
            ) {
                if (isLoadingShares) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Palette.dark
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Palette.dark
                    )
                }
            }
        }

        // Content
        if (isLoadingShares && shares.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Palette.dark
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = Copy.CatalogueShare.listLoading,
                    style = manrope(14f, JewelWeight.Regular),
                    color = Palette.muted
                )
            }
        } else if (sharesLoadError && shares.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = Copy.CatalogueShare.listOffline,
                    style = manrope(14f, JewelWeight.Regular),
                    color = Palette.muted
                )
                Spacer(modifier = Modifier.size(12.dp))
                Button(
                    onClick = { viewModel.loadShares() },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.cream),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Palette.border),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = Copy.CatalogueShare.listRefresh,
                        style = manrope(14f, JewelWeight.Semibold),
                        color = Palette.dark
                    )
                }
            }
        } else if (shares.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = Copy.CatalogueShare.listEmpty,
                    style = manrope(15f, JewelWeight.Regular),
                    color = Palette.muted
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(shares, key = { it.id }) { share ->
                    RecentShareRow(
                        share = share,
                        isRevoking = revokingShareId == share.id,
                        onRevoke = { viewModel.revokeShare(share) }
                    )
                }
            }
        }
    }
}
