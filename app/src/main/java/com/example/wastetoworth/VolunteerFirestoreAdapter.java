package com.example.wastetoworth;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wastetoworth.R;
import java.util.List;
public class VolunteerFirestoreAdapter extends RecyclerView.Adapter<VolunteerFirestoreAdapter.UserViewHolder> {
    public interface OnUserSelectedListener {
        void onUserSelected(User user);
    }
    private List<User> userList;
    private OnUserSelectedListener listener;
    public VolunteerFirestoreAdapter(List<User> userList, OnUserSelectedListener listener) {
        this.userList = userList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_volunteer_user, parent, false);
        return new UserViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.txtUserName.setText(user.getName());
        holder.txtUserType.setText(user.getType());
        holder.txtUserLocation.setText(user.getLocation());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserSelected(user);
            }
        });
    }
    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }
    public void updateList(List<User> newList) {
        userList = newList;
        notifyDataSetChanged();
    }
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtUserType, txtUserLocation;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserType = itemView.findViewById(R.id.txtUserType);
            txtUserLocation = itemView.findViewById(R.id.txtUserLocation);
        }
    }
}