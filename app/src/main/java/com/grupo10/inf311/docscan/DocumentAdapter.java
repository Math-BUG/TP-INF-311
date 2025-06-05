package com.grupo10.inf311.docscan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocViewHolder> {

    private List<Document> docList;

    public DocumentAdapter(List<Document> docList) {
        this.docList = docList;
    }

    @NonNull
    @Override
    public DocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new DocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocViewHolder holder, int position) {
        Document doc = docList.get(position);
        holder.txtDocName.setText(doc.getName());
        holder.txtDocDate.setText(doc.getDate());
    }

    @Override
    public int getItemCount() {
        return docList.size();
    }

    public static class DocViewHolder extends RecyclerView.ViewHolder {
        TextView txtDocName, txtDocDate;

        public DocViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDocName = itemView.findViewById(R.id.txtDocName);
            txtDocDate = itemView.findViewById(R.id.txtDocDate);
        }
    }
}
