package insights.water.waterinsightsv005;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

import static insights.water.waterinsightsv005.DataCollectionActivity.RESULTS_KEY;

public class ResultsListFragment extends Fragment implements View.OnClickListener {

    private ExpandableLayout expandableLayoutHardness;
    private ExpandableLayout expandableLayoutAlkalinity;
    private ExpandableLayout expandableLayoutPH;
    private ExpandableLayout expandableLayoutTotalChlorine;
    private ExpandableLayout expandableLayoutFreeChlorine;
    private ExpandableLayout expandableLayoutNitrite;
    private ExpandableLayout expandableLayoutNitrate;
    private ExpandableLayout expandableLayoutCopper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_results_list, container, false);

        expandableLayoutHardness = rootView.findViewById(R.id.expandable_layout_hardness);
        expandableLayoutAlkalinity = rootView.findViewById(R.id.expandable_layout_alkalinity);
        expandableLayoutPH = rootView.findViewById(R.id.expandable_layout_ph);
        expandableLayoutTotalChlorine = rootView.findViewById(R.id.expandable_layout_total_chlorine);
        expandableLayoutFreeChlorine = rootView.findViewById(R.id.expandable_layout_free_chlorine);
        expandableLayoutNitrite = rootView.findViewById(R.id.expandable_layout_nitrite);
        expandableLayoutNitrate = rootView.findViewById(R.id.expandable_layout_nitrate);
        expandableLayoutCopper = rootView.findViewById(R.id.expandable_layout_copper);

        expandableLayoutHardness.setOnExpansionUpdateListener(new ExpandableLayout.OnExpansionUpdateListener() {
            @Override
            public void onExpansionUpdate(float expansionFraction, int state) {
                Log.d("ExpandableLayout0", "State: " + state);
            }
        });

        expandableLayoutAlkalinity.setOnExpansionUpdateListener(new ExpandableLayout.OnExpansionUpdateListener() {
            @Override
            public void onExpansionUpdate(float expansionFraction, int state) {
                Log.d("ExpandableLayout1", "State: " + state);
            }
        });

        rootView.findViewById(R.id.expand_button_hardness).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_alkalinity).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_ph).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_total_chlorine).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_free_chlorine).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_nitrite).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_nitrate).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_copper).setOnClickListener(this);


        Bundle extras = getActivity().getIntent().getExtras();
        float[] results = extras.getFloatArray(RESULTS_KEY);

        ((TextView) rootView.findViewById(R.id.expand_button_hardness))
                .setText(getString(R.string.hardness_string) + ": " + results[0]);
        ((TextView) rootView.findViewById(R.id.expand_button_alkalinity))
                .setText(getString(R.string.alkalinity_string) + ": " + results[1]);
        ((TextView) rootView.findViewById(R.id.expand_button_ph))
                .setText(getString(R.string.ph_string) + ": " + results[2]);
        ((TextView) rootView.findViewById(R.id.expand_button_total_chlorine))
                .setText(getString(R.string.total_chlorine_string) + ": " + results[3]);
        ((TextView) rootView.findViewById(R.id.expand_button_free_chlorine))
                .setText(getString(R.string.free_chlorine_string) + ": " + results[4]);
        ((TextView) rootView.findViewById(R.id.expand_button_nitrite))
                .setText(getString(R.string.nitrite_string) + ": " + results[5]);
        ((TextView) rootView.findViewById(R.id.expand_button_nitrate))
                .setText(getString(R.string.nitrate_string) + ": " + results[6]);
        ((TextView) rootView.findViewById(R.id.expand_button_copper))
                .setText(getString(R.string.copper_string) + ": " + results[7]);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.expand_button_hardness:
                if (expandableLayoutHardness.isExpanded()) {
                    expandableLayoutHardness.collapse();
                } else {
                    expandableLayoutHardness.expand();
                }
                break;

            case R.id.expand_button_alkalinity:
                if (expandableLayoutAlkalinity.isExpanded()) {
                    expandableLayoutAlkalinity.collapse();
                } else {
                    expandableLayoutAlkalinity.expand();
                }
                break;

            case R.id.expand_button_ph:
                if (expandableLayoutPH.isExpanded()) {
                    expandableLayoutPH.collapse();
                } else {
                    expandableLayoutPH.expand();
                }
                break;

            case R.id.expand_button_total_chlorine:
                if (expandableLayoutTotalChlorine.isExpanded()) {
                    expandableLayoutTotalChlorine.collapse();
                } else {
                    expandableLayoutTotalChlorine.expand();
                }
                break;

            case R.id.expand_button_free_chlorine:
                if (expandableLayoutFreeChlorine.isExpanded()) {
                    expandableLayoutFreeChlorine.collapse();
                } else {
                    expandableLayoutFreeChlorine.expand();
                }
                break;

            case R.id.expand_button_nitrite:
                if (expandableLayoutNitrite.isExpanded()) {
                    expandableLayoutNitrite.collapse();
                } else {
                    expandableLayoutNitrite.expand();
                }
                break;

            case R.id.expand_button_nitrate:
                if (expandableLayoutNitrate.isExpanded()) {
                    expandableLayoutNitrate.collapse();
                } else {
                    expandableLayoutNitrate.expand();
                }
                break;

            case R.id.expand_button_copper:
                if (expandableLayoutCopper.isExpanded()) {
                    expandableLayoutCopper.collapse();
                } else {
                    expandableLayoutCopper.expand();
                }
                break;
        }
    }
}