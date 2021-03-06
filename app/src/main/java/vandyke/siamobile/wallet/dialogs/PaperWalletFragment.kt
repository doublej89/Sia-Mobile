/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.dialogs

import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_wallet_paper.*
import siawallet.Wallet
import vandyke.siamobile.R
import vandyke.siamobile.misc.TextTouchCopyListAdapter
import vandyke.siamobile.misc.Utils
import java.util.*

class PaperWalletFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_wallet_paper, null)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val wallet = Wallet()

        try {
            wallet.generateSeed()
        } catch (e: Exception) {
            e.printStackTrace()
            paperSeed.text = "Failed to generate seed"
            return
        }

        val seed = wallet.seed
        val addresses = ArrayList<String>()
        for (i in 0..19) {
            addresses.add(wallet.getAddress(i.toLong()))
        }

        paperSeed.setOnClickListener { v ->
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("paper wallet info", seed)
            clipboard.primaryClip = clip
            Utils.snackbar(v, "Copied seed to clipboard", Snackbar.LENGTH_SHORT)
        }
        paperSeed.text = seed

        paperAddresses.adapter = TextTouchCopyListAdapter(activity, R.layout.text_touch_copy_list_item, addresses)

        paperCopy.setOnClickListener { v ->
            val result = StringBuilder()
            result.append("Seed:\n")
            result.append(seed + "\n")
            result.append("Addresses:\n")
            for (i in addresses.indices) {
                result.append(addresses[i])
                if (i < addresses.size - 1)
                    result.append(",\n")
            }
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("paper wallet info", result.toString())
            clipboard.primaryClip = clip
            Utils.snackbar(v, "Copied seed and addresses to clipboard", Snackbar.LENGTH_SHORT)
        }
    }
}
