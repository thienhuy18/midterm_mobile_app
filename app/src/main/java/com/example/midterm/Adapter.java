package com.example.midterm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>  {

    private List<Student> students;
    private OnItemClickListener onItemClick;

    public Adapter(List<Student> students) {
        this.students = students;
    }

    // Custom listener interface for item click
    public interface OnItemClickListener {
        void onItemClick(Student student);
    }

    public void setAdapter(List<Student> students) {
        this.students = students;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClick) {
        this.onItemClick = onItemClick;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtStudentName, txtGender, txtId, txtSubject;
        CheckBox chkOption;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStudentName = itemView.findViewById(R.id.txtStudentName);
            txtGender = itemView.findViewById(R.id.txtGender);
            txtId = itemView.findViewById(R.id.txtId);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            chkOption = itemView.findViewById(R.id.chkOption);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = students.get(position);
        holder.txtStudentName.setText(student.getName());
        holder.txtGender.setText(student.getGender());
        holder.txtId.setText(student.getId());
        holder.txtSubject.setText(student.getStudy());

        holder.chkOption.setChecked(student.isOption());
        holder.chkOption.setOnCheckedChangeListener((buttonView, isChecked) -> {
            student.setOption(isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onItemClick(student);
            }
        });

    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public List<Student> getCheckedStudents() {
        List<Student> checkedStudents = new ArrayList<>();
        for (Student student : students) {
            if (student.isOption()) {
                checkedStudents.add(student);
            }
        }
        return checkedStudents;
    }
}
