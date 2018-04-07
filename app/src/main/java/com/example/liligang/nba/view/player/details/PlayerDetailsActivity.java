package com.example.liligang.nba.view.player.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.liligang.nba.R;
import com.example.liligang.nba.base.BaseActivity;
import com.example.liligang.nba.bean.player.detail.PlayerInfoResponseBean;
import com.example.liligang.nba.bean.player.detail.PlayerDetailResultSetsBean;
import com.example.liligang.nba.bean.player.detail.PlayerLogResponseBean;
import com.example.liligang.nba.constant.ConstantValue;
import com.example.liligang.nba.constant.Team;
import com.example.liligang.nba.net.NetObserver;
import com.example.liligang.nba.net.PlayerServerApi;
import com.example.liligang.nba.net.SingletonNetServer;

import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlayerDetailsActivity extends BaseActivity {
    private ImageView mIconView;
    private TextView  mNameView;
    private TextView  mIdentifierView;

    private TextView mPointsTextView;
    private TextView mAsistantTextView;
    private TextView mReboundsTextView;

    private int mPlayerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);
        mPlayerId = getIntent().getIntExtra(ConstantValue.INTENT_KEY.ID, 0);
        initView();
        getNetworkData();
    }

    private void initView() {
        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        int teamId = getIntent().getIntExtra(ConstantValue.INTENT_KEY.TEAM_ID, 0);
        Team team = Team.getFormId(teamId);
        findViewById(R.id.icon_layout).setBackgroundResource(team==null?Team.ATL.color:team.color);

        mIconView = findViewById(R.id.icon_view);
        mNameView = findViewById(R.id.name_view);
        mIdentifierView = findViewById(R.id.identifier_view);

        mPointsTextView   = findViewById(R.id.points_text_view);
        mAsistantTextView = findViewById(R.id.asistant_text_view);
        mReboundsTextView = findViewById(R.id.rebounds_text_view);

        String url = String.format(Locale.getDefault(), PlayerServerApi.PLAYER_ICON_URL_FORMAT, mPlayerId);
        Glide.with(this)
                .applyDefaultRequestOptions(new RequestOptions().circleCrop())
                .load(url).into(mIconView);
    }

    private void getNetworkData() {
        SingletonNetServer.INSTANCE.getPlayerServerApi().getPlayerInfo(mPlayerId)
                .compose(this.<PlayerInfoResponseBean>bindToLifecycle())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<PlayerInfoResponseBean>() {
                    @Override
                    public void onNext(PlayerInfoResponseBean playerDetailResponseBean) {
                        initInfoViewData(playerDetailResponseBean.getResultSets());
                    }
                });

        SingletonNetServer.INSTANCE.getPlayerServerApi().getPlayerLog(String.valueOf(mPlayerId), "2017-18")
                .compose(this.<PlayerLogResponseBean>bindToLifecycle())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<PlayerLogResponseBean>() {
                    @Override
                    public void onNext(PlayerLogResponseBean playerLogResponseBean) {

                    }
                });
    }

    private void initInfoViewData(List<PlayerDetailResultSetsBean> data) {
        if (data == null) return;

        Object[] playerInfo = data.get(0).getRowSet().get(0);
        Object[] playerHeadlineStats = data.get(1).getRowSet().get(0);

        mNameView.setText((String) playerInfo[3]);
        mIdentifierView.setText((playerInfo[28]).toString());

        mPointsTextView.setText(  String.format(Locale.getDefault(), "%.2f", playerHeadlineStats[3]));
        mAsistantTextView.setText(String.format(Locale.getDefault(), "%.2f", playerHeadlineStats[4]));
        mReboundsTextView.setText(String.format(Locale.getDefault(), "%.2f", playerHeadlineStats[5]));
    }
}