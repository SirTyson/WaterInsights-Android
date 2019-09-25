package insights.water.waterinsightsv005;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

public class ResultsListFragment extends Fragment implements View.OnClickListener {

    private ExpandableLayout expandableLayoutHardness;
    private ExpandableLayout expandableLayoutAlkalinity;
    private ExpandableLayout expandableLayoutPH;
    private ExpandableLayout expandableLayoutTotalChlorine;
    private ExpandableLayout expandableLayoutNitrite;
    private ExpandableLayout expandableLayoutNitrate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_results_list, container, false);

        expandableLayoutHardness = rootView.findViewById(R.id.expandable_layout_hardness);
        expandableLayoutAlkalinity = rootView.findViewById(R.id.expandable_layout_alkalinity);
        expandableLayoutPH = rootView.findViewById(R.id.expandable_layout_ph);
        expandableLayoutTotalChlorine = rootView.findViewById(R.id.expandable_layout_total_chlorine);
        expandableLayoutNitrite = rootView.findViewById(R.id.expandable_layout_nitrite);
        expandableLayoutNitrate = rootView.findViewById(R.id.expandable_layout_nitrate);

        rootView.findViewById(R.id.expand_button_hardness).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_alkalinity).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_ph).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_total_chlorine).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_nitrite).setOnClickListener(this);
        rootView.findViewById(R.id.expand_button_nitrate).setOnClickListener(this);

        float[] results = TakePictureActivity.getBundleResults(getActivity().getIntent().getExtras());

        ((TextView) rootView.findViewById(R.id.expand_button_hardness))
                .setText(getString(R.string.hardness_string) + ": " + results[0]);
        ((TextView) rootView.findViewById(R.id.expand_button_alkalinity))
                .setText(getString(R.string.alkalinity_string) + ": " + results[1]);
        ((TextView) rootView.findViewById(R.id.expand_button_ph))
                .setText(getString(R.string.ph_string) + ": " + results[2]);
        ((TextView) rootView.findViewById(R.id.expand_button_total_chlorine))
                .setText(getString(R.string.total_chlorine_string) + ": " + results[3]);
        ((TextView) rootView.findViewById(R.id.expand_button_nitrite))
                .setText(getString(R.string.nitrite_string) + ": " + results[5]);
        ((TextView) rootView.findViewById(R.id.expand_button_nitrate))
                .setText(getString(R.string.nitrate_string) + ": " + results[6]);

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
        }
    }
}