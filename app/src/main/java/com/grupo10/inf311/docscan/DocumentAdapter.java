package com.grupo10.inf311.docscan;
// DocumentAdapter.java
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<Document> docList;
    private OnDocumentClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Armazena a posição do item selecionado

    public DocumentAdapter(List<Document> docList, OnDocumentClickListener listener) {
        this.docList = docList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document currentDoc = docList.get(position);
        holder.txtDocName.setText(currentDoc.getName());
        holder.txtDocDate.setText(currentDoc.getDate());

        if (position == selectedPosition) {
            holder.cardViewDocument.setCardBackgroundColor(Color.LTGRAY);
        } else {
            holder.cardViewDocument.setCardBackgroundColor(Color.WHITE);
        }

        holder.cardViewDocument.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();

            if (clickedPosition == RecyclerView.NO_POSITION) {
                return;
            }

            int previousSelectedPosition = selectedPosition;

            if (clickedPosition == selectedPosition) {
                // If the same item is clicked, deselect it
                selectedPosition = RecyclerView.NO_POSITION;
            } else {
                // Select the new item
                selectedPosition = clickedPosition;
            }

            // Notify old selected item if it was valid
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition);
            }
            // Notify current selected item
            notifyItemChanged(clickedPosition);

            if (listener != null) {
                Document clickedDoc = docList.get(clickedPosition);
                listener.onDocumentClick(clickedDoc.getId(), clickedPosition == selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return docList.size();
    }

    // New method to add a document
    public void addDocument(int position, Document document) {
        docList.add(position, document);
        // If an item was selected, and the new item is inserted before it,
        // we need to increment the selectedPosition to keep track of the *same* item.
        if (selectedPosition != RecyclerView.NO_POSITION && position <= selectedPosition) {
            selectedPosition++; // Shift the selected position
        }
        notifyItemInserted(position);
    }

    // Method to remove a document (good practice to include for completeness)
    public void removeDocument(int position) {
        if (position == RecyclerView.NO_POSITION || position >= docList.size()) {
            return;
        }
        docList.remove(position);

        // If the removed item was the selected one
        if (position == selectedPosition) {
            selectedPosition = RecyclerView.NO_POSITION; // No item is selected now
        }
        // If the removed item was before the selected one, shift selectedPosition down
        else if (position < selectedPosition) {
            selectedPosition--;
        }
        notifyItemRemoved(position);
    }


    public static class DocumentViewHolder extends RecyclerView.ViewHolder {
        public TextView txtDocName;
        public TextView txtDocDate;
        public CardView cardViewDocument;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDocName = itemView.findViewById(R.id.txtDocName);
            txtDocDate = itemView.findViewById(R.id.txtDocDate);
            cardViewDocument = itemView.findViewById(R.id.cardViewDocument);
        }
    }

    public interface OnDocumentClickListener {
        void onDocumentClick(String documentId, boolean isSelected);
    }

    public String getSelectedDocumentId() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < docList.size()) {
            return docList.get(selectedPosition).getId();
        }
        return null;
    }
}