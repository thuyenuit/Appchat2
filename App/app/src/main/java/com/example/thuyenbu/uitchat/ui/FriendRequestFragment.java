package com.example.thuyenbu.uitchat.ui;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.thuyenbu.uitchat.R;
import com.example.thuyenbu.uitchat.data.FriendDB;
import com.example.thuyenbu.uitchat.data.RequestDB;
import com.example.thuyenbu.uitchat.data.StaticConfig;
import com.example.thuyenbu.uitchat.model.Friend;
import com.example.thuyenbu.uitchat.model.FriendRequest;
import com.example.thuyenbu.uitchat.model.ListFriend;
import com.example.thuyenbu.uitchat.model.ListFriendRequest;
import com.example.thuyenbu.uitchat.service.ServiceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendRequestFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerListFrendRequest;
    private ListFriendRequestAdapter adapter;
    public FragFriendClickFloatButton onClickFloatButton;
    private ListFriendRequest dataListFriendRequest = null;
    private ArrayList<String> listFriendID = null;
    private LovelyProgressDialog dialogFindAllFriend;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CountDownTimer detectFriendOnline;
    public static int ACTION_START_CHAT = 1;

    public static final String ACTION_DELETE_FRIEND = "com.android.rivchat.DELETE_REQUEST";

    private BroadcastReceiver deleteFriendReceiver;

    public FriendRequestFragment() {

        onClickFloatButton = new FragFriendClickFloatButton();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        detectFriendOnline = new CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
               // ServiceUtils.updateFriendStatus(getContext(), dataListFriend);
                // ServiceUtils.updateUserStatus(getContext());
            }

            @Override
            public void onFinish() {

            }
        };

        //RequestDB.getInstance(getContext()).dropDB();
        if (dataListFriendRequest == null) {
            dataListFriendRequest = RequestDB.getInstance(getContext()).getListFriendRequest(StaticConfig.UID);
            if (dataListFriendRequest.getListRequest().size() > 0) {
                listFriendID = new ArrayList<>();
                for (FriendRequest request : dataListFriendRequest.getListRequest()) {
                    listFriendID.add(request.id);
                }
                detectFriendOnline.start();
            }
        }

        Log.d(TAG, "Danh sach yeu cau: " + dataListFriendRequest.getListRequest().size());

        View layout = inflater.inflate(R.layout.fragment_friend_request, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListFrendRequest = (RecyclerView) layout.findViewById(R.id.recycleListFriendRequest);
        recyclerListFrendRequest.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        adapter = new ListFriendRequestAdapter(getContext(), dataListFriendRequest, this);
        recyclerListFrendRequest.setAdapter(adapter);
        dialogFindAllFriend = new LovelyProgressDialog(getContext());

        if (listFriendID == null) {
            listFriendID = new ArrayList<>();
            /*dialogFindAllFriend.setCancelable(false).setTitle("Đang tải dữ liệu")
                    .setMessage("Vui lòng chờ trong giây lát!")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();*/
            getListRequestUId();
        }


        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                for (FriendRequest request : dataListFriendRequest.getListRequest()) {
                    if(idDeleted.equals(request.id)){
                        ArrayList<FriendRequest> friends = dataListFriendRequest.getListRequest();
                        friends.remove(request);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);

        return layout;
    }

    @Override
    public void onDestroyView (){
        super.onDestroyView();
        getContext().unregisterReceiver(deleteFriendReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTION_START_CHAT == requestCode && data != null && ListFriendRequestAdapter.mapMark != null) {
            ListFriendRequestAdapter.mapMark.put(data.getStringExtra("idFriend"), false);
        }
    }

    @Override
    public void onRefresh() {
        listFriendID.clear();
        dataListFriendRequest.getListRequest().clear();
        adapter.notifyDataSetChanged();
        RequestDB.getInstance(getContext()).dropDB();
        detectFriendOnline.cancel();
        getListRequestUId();
    }

    public class FragFriendClickFloatButton implements View.OnClickListener {
        Context context;
        LovelyProgressDialog dialogWait;

        public FragFriendClickFloatButton() {
        }

        public FragFriendClickFloatButton getInstance(Context context) {
            this.context = context;
            dialogWait = new LovelyProgressDialog(context);
            return this;
        }

        @Override
        public void onClick(final View view) {
            new LovelyTextInputDialog(view.getContext(), R.style.EditTextTintTheme)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Add friend")
                    .setMessage("Enter friend email")
                    .setIcon(R.drawable.ic_add_friend)
                    .setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                    .setInputFilter("Email not found", new LovelyTextInputDialog.TextFilter() {
                        @Override
                        public boolean check(String text) {
                            Pattern VALID_EMAIL_ADDRESS_REGEX =
                                    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
                            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(text);
                            return matcher.find();
                        }
                    })
                    .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                        @Override
                        public void onTextInputConfirmed(String text) {
                            //Tim id user id
                            findIDEmail(text);
                            //Check xem da ton tai ban ghi friend chua
                            //Ghi them 1 ban ghi
                        }
                    })
                    .show();
        }

        /**
         * TIm id cua email tren server
         *
         * @param email
         */
        private void findIDEmail(String email) {
//            dialogWait.setCancelable(false)
//                    .setIcon(R.drawable.ic_add_friend)
//                    .setTitle("Finding friend....")
//                    .setTopColorRes(R.color.colorPrimary)
//                    .show();
//            FirebaseDatabase.getInstance().getReference().child("user").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    dialogWait.dismiss();
//                    if (dataSnapshot.getValue() == null) {
//                        //email not found
//                        new LovelyInfoDialog(context)
//                                .setTopColorRes(R.color.colorAccent)
//                                .setIcon(R.drawable.ic_add_friend)
//                                .setTitle("Fail")
//                                .setMessage("Email not found")
//                                .show();
//                    } else {
//                        String id = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
//                        if (id.equals(StaticConfig.UID)) {
//                            new LovelyInfoDialog(context)
//                                    .setTopColorRes(R.color.colorAccent)
//                                    .setIcon(R.drawable.ic_add_friend)
//                                    .setTitle("Fail")
//                                    .setMessage("Email not valid")
//                                    .show();
//                        } else {
//                            HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(id);
//                            Friend user = new Friend();
//                            user.name = (String) userMap.get("name");
//                            user.email = (String) userMap.get("email");
//                            user.avata = (String) userMap.get("avata");
//                            user.id = id;
//                            user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
//                            checkBeforAddFriend(id, user);
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
        }

        /**
         * Lay danh sach friend cua một UID
         */
        private void checkBeforAddFriend(final String idFriend, FriendRequest userInfo) {
            dialogWait.setCancelable(false)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Add friend....")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();

            //Check xem da ton tai id trong danh sach id chua
            if (listFriendID.contains(idFriend)) {
                dialogWait.dismiss();
                new LovelyInfoDialog(context)
                        .setTopColorRes(R.color.colorPrimary)
                        .setIcon(R.drawable.ic_add_friend)
                        .setTitle("Friend")
                        .setMessage("User "+userInfo.email + " has been friend")
                        .show();
            } else {
                //addFriend(idFriend, true);
                //listFriendID.add(idFriend);
                //dataListFriendRequest.getListRequest().add(userInfo);
                //FriendDB.getInstance(getContext()).addFriend(userInfo);
               // adapter.notifyDataSetChanged();
            }
        }

        /**
         * Add friend
         *
         * @param idFriend
         */
        private void addFriend(final String idFriend, boolean isIdFriend) {
            if (idFriend != null) {
                if (isIdFriend) {
                    FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).push().setValue(idFriend)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        addFriend(idFriend, false);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialogWait.dismiss();
                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.colorAccent)
                                            .setIcon(R.drawable.ic_add_friend)
                                            .setTitle("False")
                                            .setMessage("False to add friend success")
                                            .show();
                                }
                            });
                } else {
                    FirebaseDatabase.getInstance().getReference().child("friend/" + idFriend).push().setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addFriend(null, false);
                            }
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialogWait.dismiss();
                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.colorAccent)
                                            .setIcon(R.drawable.ic_add_friend)
                                            .setTitle("False")
                                            .setMessage("False to add friend success")
                                            .show();
                                }
                            });
                }
            } else {
                dialogWait.dismiss();
                new LovelyInfoDialog(context)
                        .setTopColorRes(R.color.colorPrimary)
                        .setIcon(R.drawable.ic_add_friend)
                        .setTitle("Success")
                        .setMessage("Add friend success")
                        .show();
            }
        }

    }

    /**
     * Lay danh sach yeu cau ket ban tren server
     */
    private void getListRequestUId() {
        FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID + "/listRequest").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();
                        listFriendID.add(mapRecord.get(key).toString());
                    }
                    getAllFriendInfo(0);

                } else {
                    dialogFindAllFriend.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Truy cap bang user lay thong tin id nguoi dung
     */
    private void getAllFriendInfo(final int index) {
        if (index == listFriendID.size()) {
            //save list friend
            adapter.notifyDataSetChanged();
            dialogFindAllFriend.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
            detectFriendOnline.start();
        } else {

            final String id = listFriendID.get(index);
            FirebaseDatabase.getInstance().getReference().child("user/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        FriendRequest user = new FriendRequest();
                        HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                        user.email = (String) mapUserInfo.get("email");
                        user.id = id;

                        dataListFriendRequest.getListRequest().add(user);
                        RequestDB.getInstance(getContext()).addFriendRequest(user, StaticConfig.UID);
                    }
                    getAllFriendInfo(index + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
}

class ListFriendRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ListFriendRequest listRequest;
    private Context context;
    public static Map<String, Query> mapQuery;
    public static Map<String, ChildEventListener> mapChildListener;
    public static Map<String, Boolean> mapMark;
    private FriendRequestFragment fragment;
    LovelyProgressDialog dialogWaitDeleting;

    public ListFriendRequestAdapter(Context context, ListFriendRequest listRequest, FriendRequestFragment fragment) {
        this.listRequest = listRequest;
        this.context = context;
        mapQuery = new HashMap<>();
        mapChildListener = new HashMap<>();
        mapMark = new HashMap<>();
        this.fragment = fragment;
        dialogWaitDeleting = new LovelyProgressDialog(context);

        Log.d(TAG, "Danh sach yeu cau: " + listRequest.getListRequest());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend_request, parent, false);
        return new ItemFriendRequestViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final String name = listRequest.getListRequest().get(position).email;
        final String id = listRequest.getListRequest().get(position).id;
        final String avata = listRequest.getListRequest().get(position).avata;
        ((ItemFriendRequestViewHolder) holder).txtNameNew.setText(name);

        //nhấn giữ để xóa bạn
//        ((View) ((ItemFriendRequestViewHolder) holder).txtNameNew.getParent().getParent().getParent())
//                .setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View view) {
//                        String friendName = (String)((ItemFriendRequestViewHolder) holder).txtNameNew.getText();
//
//                        new AlertDialog.Builder(context)
//                                .setTitle("Delete Friend")
//                                .setMessage("Are you sure want to delete "+friendName+ "?")
//                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        dialogInterface.dismiss();
//                                        final String idFriendRemoval = listRequest.getListRequest().get(position).id;
//                                        dialogWaitDeleting.setTitle("Deleting...")
//                                                .setCancelable(false)
//                                                .setTopColorRes(R.color.colorAccent)
//                                                .show();
//                                        deleteFriend(idFriendRemoval);
//                                    }
//                                })
//                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        dialogInterface.dismiss();
//                                    }
//                                }).show();
//
//                        return true;
//                    }
//                });


//        if (listRequest.getListRequest().get(position).message.text.length() > 0) {
//            ((ItemFriendRequestViewHolder) holder).txtMessageNew.setVisibility(View.VISIBLE);
//            //((ItemFriendRequestViewHolder) holder).txtTime.setVisibility(View.VISIBLE);
//            if (!listRequest.getListRequest().get(position).message.text.startsWith(id)) {
//                ((ItemFriendRequestViewHolder) holder).txtMessageNew.setText(listRequest.getListRequest().get(position).message.text);
//                ((ItemFriendRequestViewHolder) holder).txtMessageNew.setTypeface(Typeface.DEFAULT);
//                ((ItemFriendRequestViewHolder) holder).txtNameNew.setTypeface(Typeface.DEFAULT);
//            } else {
//                ((ItemFriendRequestViewHolder) holder).txtMessageNew.setText(listRequest.getListRequest().get(position).message.text.substring((id + "").length()));
//                ((ItemFriendRequestViewHolder) holder).txtMessageNew.setTypeface(Typeface.DEFAULT_BOLD);
//                ((ItemFriendRequestViewHolder) holder).txtNameNew.setTypeface(Typeface.DEFAULT_BOLD);
//            }
//            String time = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(listRequest.getListRequest().get(position).message.timestamp));
//            String today = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));
//
//            /*if (today.equals(time)) {
//                ((ItemFriendRequestViewHolder) holder).txtTime.setText(new SimpleDateFormat("HH:mm").format(new Date(listRequest.getListRequest().get(position).message.timestamp)));
//            } else {
//                ((ItemFriendRequestViewHolder) holder).txtTime.setText(new SimpleDateFormat("MMM d").format(new Date(listRequest.getListRequest().get(position).message.timestamp)));
//            }*/
//        } else {
//            ((ItemFriendRequestViewHolder) holder).txtMessageNew.setVisibility(View.GONE);
//            //((ItemFriendRequestViewHolder) holder).txtTime.setVisibility(View.GONE);
//            if (mapQuery.get(id) == null && mapChildListener.get(id) == null) {
//
//            } else {
//                mapQuery.get(id).removeEventListener(mapChildListener.get(id));
//                mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
//                mapMark.put(id, true);
//            }
//        }

        if (listRequest.getListRequest().get(position).avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            ((ItemFriendRequestViewHolder) holder).avataNew.setImageResource(R.drawable.default_avata);
        } else {
            byte[] decodedString = Base64.decode(listRequest.getListRequest().get(position).avata, Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ((ItemFriendRequestViewHolder) holder).avataNew.setImageBitmap(src);
        }

        if (listRequest.getListRequest().get(position).status.isOnline) {
            ((ItemFriendRequestViewHolder) holder).avataNew.setBorderWidth(10);
        } else {
            ((ItemFriendRequestViewHolder) holder).avataNew.setBorderWidth(0);
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "Danh sach: " + listRequest);
        return  0;
        //return listRequest != null ?
        //        (listRequest.getListRequest() != null ? listRequest.getListRequest().size() : 0) : 0;
    }

   /* @Override
    public int getItemCountRequest() {
        return listRequest.getListRequest() != null ? listRequest.getListRequest().size() : 0;
    }*/

    /**
     * Delete friend
     *
     * @param idFriend
     */
    private void deleteFriend(final String idFriend) {
        if (idFriend != null) {
            FirebaseDatabase.getInstance().getReference().child("friend").child(StaticConfig.UID)
                    .orderByValue().equalTo(idFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() == null) {
                        //email not found
                        dialogWaitDeleting.dismiss();
                        new LovelyInfoDialog(context)
                                .setTopColorRes(R.color.colorAccent)
                                .setTitle("Error")
                                .setMessage("Error occurred during deleting friend")
                                .show();
                    } else {
                        String idRemoval = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                        FirebaseDatabase.getInstance().getReference().child("friend")
                                .child(StaticConfig.UID).child(idRemoval).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialogWaitDeleting.dismiss();

                                        new LovelyInfoDialog(context)
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Success")
                                                .setMessage("Friend deleting successfully")
                                                .show();

                                        Intent intentDeleted = new Intent(FriendsFragment.ACTION_DELETE_FRIEND);
                                        intentDeleted.putExtra("idFriend", idFriend);
                                        context.sendBroadcast(intentDeleted);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialogWaitDeleting.dismiss();
                                        new LovelyInfoDialog(context)
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Error")
                                                .setMessage("Error occurred during deleting friend")
                                                .show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            dialogWaitDeleting.dismiss();
            new LovelyInfoDialog(context)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Error")
                    .setMessage("Error occurred during deleting friend")
                    .show();
        }
    }
}

class ItemFriendRequestViewHolder extends RecyclerView.ViewHolder{
    public CircleImageView avataNew;
    public TextView txtNameNew, txtMessageNew;
    private Context context;

    ItemFriendRequestViewHolder(Context context, View itemView) {
        super(itemView);
        avataNew = (CircleImageView) itemView.findViewById(R.id.icon_avata_user_new);
        txtNameNew = (TextView) itemView.findViewById(R.id.txtNameNew);
       // txtTime = (TextView) itemView.findViewById(R.id.txtTime);
        txtMessageNew = (TextView) itemView.findViewById(R.id.txtMessageNew);
        this.context = context;
    }
}
