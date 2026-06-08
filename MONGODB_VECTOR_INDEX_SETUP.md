# MongoDB Atlas Vector Search Index Setup

To use the RAG chatbot, you need to create a vector search index on your MongoDB Atlas collection.

## Step 1: Create the index in MongoDB Atlas

1. Go to your MongoDB Atlas dashboard
2. Navigate to your cluster → "Database" → "Collections"
3. Select the `monolith` database and `document_chunks` collection
4. Click on "Search Indexes" → "Create Search Index"
5. Choose "JSON Editor"
6. Paste the following index definition:

```json
{
  "mappings": {
    "dynamic": true,
    "fields": {
      "embedding": {
        "dimensions": 384,
        "similarity": "cosine",
        "type": "knnVector"
      }
    }
  }
}
```

7. Name the index `vector_index` (must match the index name in DocumentChunkRepository.java)
8. Click "Create Search Index" and wait for it to be ready

## Notes

- The dimensions (384) match the output of the sentence-transformers/all-MiniLM-L6-v2 model used by Spring AI Transformers
- The similarity function is cosine
- The index name must exactly be `vector_index`

