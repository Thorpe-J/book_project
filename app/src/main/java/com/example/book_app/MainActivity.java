package com.example.book_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
public static final int ADD_BOOK_REQUEST = 1;
public static final int EDIT_BOOK_REQUEST = 2;

    private BookViewModel bookViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton buttonAddBook = findViewById(R.id.button_add_book);
        buttonAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditBookActivity.class);
                startActivityForResult(intent,ADD_BOOK_REQUEST);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final BookAdapter adapter = new BookAdapter();
        recyclerView.setAdapter(adapter);


        bookViewModel = new ViewModelProvider(this
                , ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(BookViewModel.class);

        bookViewModel.getAllBooks().observe(this, new Observer<List<Book>>() {
            @Override
            public void onChanged(List<Book> books) {
                adapter.setBooks(books);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                bookViewModel.delete(adapter.getBookAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Book deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                Intent intent = new Intent(MainActivity.this, AddEditBookActivity.class);
                intent.putExtra(AddEditBookActivity.EXTRA_ID, book.getId());
                intent.putExtra(AddEditBookActivity.EXTRA_TITLE, book.getTitle());
                intent.putExtra(AddEditBookActivity.EXTRA_AUTHOR, book.getAuthor());
                intent.putExtra(AddEditBookActivity.EXTRA_STATUS, book.getState());
                intent.putExtra(AddEditBookActivity.EXTRA_PAGES, book.getPage_count());
                startActivityForResult(intent, EDIT_BOOK_REQUEST);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==ADD_BOOK_REQUEST && resultCode==RESULT_OK) {
            String title = data.getStringExtra(AddEditBookActivity.EXTRA_TITLE);
            String author = data.getStringExtra(AddEditBookActivity.EXTRA_AUTHOR);
            String status = data.getStringExtra(AddEditBookActivity.EXTRA_STATUS);
            int pages = data.getIntExtra(AddEditBookActivity.EXTRA_PAGES, 0);

            Book book = new Book(title, author, status, pages);
            bookViewModel.insert(book);

            Toast.makeText(this, "Book Saved", Toast.LENGTH_SHORT).show();
        } else if (requestCode==EDIT_BOOK_REQUEST && resultCode==RESULT_OK) {
            int id = data.getIntExtra(AddEditBookActivity.EXTRA_ID, -1);
            if(id==-1) {
                Toast.makeText(this, "Cannot be updated!", Toast.LENGTH_SHORT).show();
            }
            String title = data.getStringExtra(AddEditBookActivity.EXTRA_TITLE);
            String author = data.getStringExtra(AddEditBookActivity.EXTRA_AUTHOR);
            String status = data.getStringExtra(AddEditBookActivity.EXTRA_STATUS);
            int pages = data.getIntExtra(AddEditBookActivity.EXTRA_PAGES, 0);

            Book book = new Book(title, author, status, pages);
            book.setId(id);
            bookViewModel.update(book);

            Toast.makeText(this, "Book updated", Toast.LENGTH_SHORT).show();

        }
        else {
            Toast.makeText(this, "Failed to save!", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_books:
                bookViewModel.deleteAllBooks();
                Toast.makeText(this, "All books deleted", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.check_delete:
                if (item.isChecked()) {
                    item.setChecked(false);
                }
                else {
                    item.setChecked(true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}