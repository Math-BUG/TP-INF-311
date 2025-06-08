package com.grupo10.inf311.docscan;
// DocumentAdapter.java
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Importe ImageView
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<Document> docList;
    private OnDocumentClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

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

        holder.txtDocPages.setText("1 page");


        // Carregar imagem usando Glide
        if (currentDoc.getImagePath() != null && !currentDoc.getImagePath().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentDoc.getImagePath())
                    .placeholder(R.drawable.menu36); // Imagem padrão enquanto carrega
                    /*.error(R.drawable.menu36)*/ // Imagem padrão em caso de erro
                   /* .into(holder.imgDocThumbnail));*/
        } else {
            holder.imgDocThumbnail.setImageResource(R.drawable.menu36); // Se não tiver caminho
        }

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

            // Lógica de seleção (se for single selection)
            int previousSelectedPosition = selectedPosition;
            if (clickedPosition == selectedPosition) {
                selectedPosition = RecyclerView.NO_POSITION;
            } else {
                selectedPosition = clickedPosition;
            }

            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition);
            }
            notifyItemChanged(clickedPosition);

            if (listener != null) {
                Document clickedDoc = docList.get(clickedPosition);
                // Ao clicar no documento, chamar o listener para ir para a tela de ações
                listener.onDocumentClick(clickedDoc.getId(), clickedPosition == selectedPosition); // Mantenha isso para seleção
                listener.onDocumentAction(clickedDoc.getId()); // NOVO: Chamar para tela de ações
            }
        });
    }

    @Override
    public int getItemCount() {
        return docList.size();
    }

    public static class DocumentViewHolder extends RecyclerView.ViewHolder {
        public TextView txtDocName;
        public TextView txtDocDate;
        public TextView txtDocPages; // Referência para o TextView de páginas
        public ImageView imgDocThumbnail; // Referência para a ImageView
        public CardView cardViewDocument;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDocName = itemView.findViewById(R.id.txtDocName);
            txtDocDate = itemView.findViewById(R.id.txtDocDate);
            txtDocPages = itemView.findViewById(R.id.txtDocPages); // Inicialize
            imgDocThumbnail = itemView.findViewById(R.id.imgDocThumbnail); // Inicialize
            cardViewDocument = itemView.findViewById(R.id.cardViewDocument);
        }
    }

    public interface OnDocumentClickListener {
        void onDocumentClick(String documentId, boolean isSelected); // Para seleção visual
        void onDocumentAction(String documentId); // Para levar à tela de ações
    }

    public void addDocument(int position, Document document) {
        docList.add(position, document);
        if (selectedPosition != RecyclerView.NO_POSITION && position <= selectedPosition) {
            selectedPosition++;
        }
        notifyItemInserted(position);
    }


    public String getSelectedDocumentId() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < docList.size()) {
            return docList.get(selectedPosition).getId();
        }
        return null;
    }
}