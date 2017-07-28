/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.links

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import vandyke.siamobile.R

class LinksFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_links, null)
        return v
    }
}
