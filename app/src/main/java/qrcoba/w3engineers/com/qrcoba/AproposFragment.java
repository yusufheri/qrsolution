package qrcoba.w3engineers.com.qrcoba;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
public class AproposFragment extends Fragment {
    private Context mContext;

    public AproposFragment() {
    }
    public static AproposFragment newInstance() {
        return new AproposFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_apropos, container, false);

    }
}
