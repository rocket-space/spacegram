package org.spacegram.ui;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.spacegram.SpaceGramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;

public class SpaceGramChatSettingsActivity extends BaseFragment {

    private static final int ITEM_CONTEXT_MENU = 1;
    private static final int ITEM_FORWARD_PRO = 2;
    private static final int ITEM_FORWARD_PRO_ENABLED = 3;
    private static final int ITEM_FORWARD_MULTIPLE = 4;
    private static final int ITEM_FORWARD_CONFIRM = 5;
    private static final int ITEM_FORWARD_DRAWING_MULTI = 6;
    private static final int ITEM_FORWARD_REPEAT_COUNT = 7;

    private UniversalRecyclerView listView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("SettingsSpaceGramChat", R.string.SettingsSpaceGramChat));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, null);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString("SettingsSpaceGramChatContextualMenu", R.string.SettingsSpaceGramChatContextualMenu)));
        items.add(UItem.asButton(
                ITEM_CONTEXT_MENU,
                R.drawable.settings_chat,
                LocaleController.getString("SettingsSpaceGramChatContextualMenu", R.string.SettingsSpaceGramChatContextualMenu),
                LocaleController.getString("SettingsSpaceGramChatContextualMenuInfo", R.string.SettingsSpaceGramChatContextualMenuInfo)
        ));
        items.add(UItem.asShadow(LocaleController.getString("SettingsSpaceGramClientSideNote", R.string.SettingsSpaceGramClientSideNote)));

        items.add(UItem.asHeader(LocaleController.getString("SettingsSpaceGramChatForwardPro", R.string.SettingsSpaceGramChatForwardPro)));
        items.add(UItem.asCheck(
                ITEM_FORWARD_PRO_ENABLED,
                LocaleController.getString("SettingsSpaceGramChatForwardProEnabled", R.string.SettingsSpaceGramChatForwardProEnabled)
        ).setChecked(SpaceGramConfig.forwardProEnabled));
        items.add(UItem.asCheck(
                ITEM_FORWARD_MULTIPLE,
                LocaleController.getString("SettingsSpaceGramChatForwardMultiple", R.string.SettingsSpaceGramChatForwardMultiple)
        ).setChecked(SpaceGramConfig.forwardProMultipleOption));
        items.add(UItem.asCheck(
                ITEM_FORWARD_CONFIRM,
                LocaleController.getString("SettingsSpaceGramChatForwardConfirmAlert", R.string.SettingsSpaceGramChatForwardConfirmAlert)
        ).setChecked(SpaceGramConfig.forwardProConfirmAlert));
        items.add(UItem.asCheck(
                ITEM_FORWARD_DRAWING_MULTI,
                LocaleController.getString("SettingsSpaceGramChatForwardDrawingMulti", R.string.SettingsSpaceGramChatForwardDrawingMulti)
        ).setChecked(SpaceGramConfig.forwardProDrawingMulti));
        items.add(UItem.asSettingsCell(
                ITEM_FORWARD_REPEAT_COUNT,
                0,
                LocaleController.getString("SettingsSpaceGramChatForwardRepeatCount", R.string.SettingsSpaceGramChatForwardRepeatCount),
                String.valueOf(SpaceGramConfig.forwardProRepeatCount)
        ));
        items.add(UItem.asButton(
                ITEM_FORWARD_PRO,
                R.drawable.msg_customize,
                LocaleController.getString("SettingsSpaceGramChatForwardPro", R.string.SettingsSpaceGramChatForwardPro),
                LocaleController.getString("SettingsSpaceGramChatForwardProInfo", R.string.SettingsSpaceGramChatForwardProInfo)
        ));
        items.add(UItem.asShadow(LocaleController.getString("SettingsSpaceGramClientSideNote", R.string.SettingsSpaceGramClientSideNote)));
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ITEM_CONTEXT_MENU) {
            showContextualMenuOptions();
            return;
        }
        if (item.id == ITEM_FORWARD_PRO) {
            showForwardProOptions();
            return;
        }
        if (item.id == ITEM_FORWARD_PRO_ENABLED) {
            SpaceGramConfig.forwardProEnabled = !SpaceGramConfig.forwardProEnabled;
        } else if (item.id == ITEM_FORWARD_MULTIPLE) {
            SpaceGramConfig.forwardProMultipleOption = !SpaceGramConfig.forwardProMultipleOption;
        } else if (item.id == ITEM_FORWARD_CONFIRM) {
            SpaceGramConfig.forwardProConfirmAlert = !SpaceGramConfig.forwardProConfirmAlert;
        } else if (item.id == ITEM_FORWARD_DRAWING_MULTI) {
            SpaceGramConfig.forwardProDrawingMulti = !SpaceGramConfig.forwardProDrawingMulti;
        } else if (item.id == ITEM_FORWARD_REPEAT_COUNT) {
            SpaceGramConfig.forwardProRepeatCount++;
            if (SpaceGramConfig.forwardProRepeatCount > 20) {
                SpaceGramConfig.forwardProRepeatCount = 1;
            }
        }
        SpaceGramConfig.saveConfig();
        listView.adapter.update(true);
    }

    private void showContextualMenuOptions() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }

        ScrollView scrollView = new ScrollView(context);
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(container, new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        addContextToggle(container,
                LocaleController.getString("SettingsSpaceGramContextShowId", R.string.SettingsSpaceGramContextShowId),
                SpaceGramConfig.contextShowUserId,
                value -> SpaceGramConfig.contextShowUserId = value);
        addContextToggle(container,
                LocaleController.getString("SettingsSpaceGramContextSaveMedia", R.string.SettingsSpaceGramContextSaveMedia),
                SpaceGramConfig.contextSaveMediaQuick,
                value -> SpaceGramConfig.contextSaveMediaQuick = value);
        addContextToggle(container,
                LocaleController.getString("SettingsSpaceGramContextCopyMessageLink", R.string.SettingsSpaceGramContextCopyMessageLink),
                SpaceGramConfig.contextCopyMessageLink,
                value -> SpaceGramConfig.contextCopyMessageLink = value);
        addContextToggle(container,
                LocaleController.getString("SettingsSpaceGramContextPinShortcuts", R.string.SettingsSpaceGramContextPinShortcuts),
                SpaceGramConfig.contextPinShortcuts,
                value -> SpaceGramConfig.contextPinShortcuts = value);
        addContextToggle(container,
                LocaleController.getString("SettingsSpaceGramContextForwardProToggle", R.string.SettingsSpaceGramContextForwardProToggle),
                SpaceGramConfig.contextForwardProToggle,
                value -> SpaceGramConfig.contextForwardProToggle = value);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("SettingsSpaceGramContextMenuDialogTitle", R.string.SettingsSpaceGramContextMenuDialogTitle));
        builder.setView(scrollView);
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog, which) -> {
            SpaceGramConfig.saveConfig();
            listView.adapter.update(true);
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showForwardProOptions() {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }

        ScrollView scrollView = new ScrollView(context);
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(container, new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        addContextToggle(container,
                LocaleController.getString("SettingsSpaceGramChatForwardProEnabled", R.string.SettingsSpaceGramChatForwardProEnabled),
                SpaceGramConfig.forwardProEnabled,
                value -> SpaceGramConfig.forwardProEnabled = value);
        addContextToggle(container,
                LocaleController.getString("SettingsSpaceGramChatForwardMultiple", R.string.SettingsSpaceGramChatForwardMultiple),
                SpaceGramConfig.forwardProMultipleOption,
                value -> SpaceGramConfig.forwardProMultipleOption = value);
        addContextToggle(container,
                LocaleController.getString("SettingsSpaceGramChatForwardConfirmAlert", R.string.SettingsSpaceGramChatForwardConfirmAlert),
                SpaceGramConfig.forwardProConfirmAlert,
                value -> SpaceGramConfig.forwardProConfirmAlert = value);
        addContextToggle(container,
                LocaleController.getString("SettingsSpaceGramChatForwardDrawingMulti", R.string.SettingsSpaceGramChatForwardDrawingMulti),
                SpaceGramConfig.forwardProDrawingMulti,
                value -> SpaceGramConfig.forwardProDrawingMulti = value);

        TextCheckCell repeatCell = new TextCheckCell(context);
        repeatCell.setBackground(Theme.getSelectorDrawable(false));
        repeatCell.setTextAndCheck(
                LocaleController.getString("SettingsSpaceGramChatForwardRepeatCount", R.string.SettingsSpaceGramChatForwardRepeatCount) + ": " + SpaceGramConfig.forwardProRepeatCount,
                false,
                false
        );
        repeatCell.setOnClickListener(v -> {
            SpaceGramConfig.forwardProRepeatCount = SpaceGramConfig.clampRepeatCount(SpaceGramConfig.forwardProRepeatCount + 1);
            if (SpaceGramConfig.forwardProRepeatCount >= 20) {
                SpaceGramConfig.forwardProRepeatCount = 1;
            }
            repeatCell.setTextAndCheck(
                    LocaleController.getString("SettingsSpaceGramChatForwardRepeatCount", R.string.SettingsSpaceGramChatForwardRepeatCount) + ": " + SpaceGramConfig.forwardProRepeatCount,
                    false,
                    false
            );
        });
        container.addView(repeatCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("SettingsSpaceGramChatForwardPro", R.string.SettingsSpaceGramChatForwardPro));
        builder.setView(scrollView);
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog, which) -> {
            SpaceGramConfig.forwardProRepeatCount = SpaceGramConfig.clampRepeatCount(SpaceGramConfig.forwardProRepeatCount);
            SpaceGramConfig.saveConfig();
            listView.adapter.update(true);
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private interface ToggleListener {
        void onChanged(boolean value);
    }

    private void addContextToggle(LinearLayout container, String text, boolean initialValue, ToggleListener listener) {
        Context context = container.getContext();
        TextCheckCell cell = new TextCheckCell(context);
        cell.setBackground(Theme.getSelectorDrawable(false));
        cell.setTextAndCheck(text, initialValue, false);
        cell.setOnClickListener(v -> {
            boolean newValue = !cell.isChecked();
            cell.setChecked(newValue);
            listener.onChanged(newValue);
        });
        container.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }
}
