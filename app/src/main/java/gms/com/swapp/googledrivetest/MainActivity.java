package gms.com.swapp.googledrivetest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class MainActivity extends Activity {
    static final int REQUEST_ACCOUNT_PICKER_SAVE = 1;
    static final int REQUEST_ACCOUNT_PICKER_LOAD = 2;
    static final int REQUEST_AUTHORIZATION_SAVE = 3;
    static final int REQUEST_AUTHORIZATION_LOAD = 4;
    static final int REQUEST_GET_ACCOUNTS_SAVE = 5;
    static final int REQUEST_GET_ACCOUNTS_LOAD = 6;

    static final String FILE_TITLE = "test.txt";

    private Drive service = null;
    private GoogleAccountCredential credential = null;

    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     }

    @Override
    protected void onStart() {
        super.onStart();

        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 権限確認
                if(checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.permission_dialog_title))
                        .setMessage(getString(R.string.permission_dialog_message))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_GET_ACCOUNTS_SAVE);
                            }
                        })
                        .show();
                } else{
                    if (service == null) {
                        credential = GoogleAccountCredential.usingOAuth2(MainActivity.this, Arrays.asList(DriveScopes.DRIVE));
                        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER_SAVE);
                    } else {
                        saveTextToDrive();
                    }
                }
            }
        });

        findViewById(R.id.loadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 権限確認
                if(checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.permission_dialog_title))
                        .setMessage(getString(R.string.permission_dialog_message))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_GET_ACCOUNTS_LOAD);
                            }
                        })
                        .show();
                } else{
                    if (service == null) {
                        credential = GoogleAccountCredential.usingOAuth2(MainActivity.this, Arrays.asList(DriveScopes.DRIVE));
                        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER_LOAD);
                    } else {
                        loadTextFromDrive();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER_SAVE:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        service = getDriveService(credential);
                    }

                    saveTextToDrive();
                }
                break;
            case REQUEST_ACCOUNT_PICKER_LOAD:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        service = getDriveService(credential);
                    }

                    loadTextFromDrive();
                }
                break;
            case REQUEST_AUTHORIZATION_SAVE:
                if (resultCode == Activity.RESULT_OK) {
                    saveTextToDrive();
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER_SAVE);
                }
                break;
            case REQUEST_AUTHORIZATION_LOAD:
                if (resultCode == Activity.RESULT_OK) {
                    loadTextFromDrive();
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER_LOAD);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GET_ACCOUNTS_SAVE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可されたとき
                    if (service == null) {
                        credential = GoogleAccountCredential.usingOAuth2(MainActivity.this, Arrays.asList(DriveScopes.DRIVE));
                        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER_SAVE);
                    } else {
                        saveTextToDrive();
                    }
                } else{
                    // 許可されなかったとき
                }
                break;
            case REQUEST_GET_ACCOUNTS_LOAD:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可されたとき
                    if (service == null) {
                        credential = GoogleAccountCredential.usingOAuth2(MainActivity.this, Arrays.asList(DriveScopes.DRIVE));
                        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER_LOAD);
                    } else {
                        loadTextFromDrive();
                    }
                } else{
                    // 許可されなかったとき
                }
                break;
        }
    }

    private void saveTextToDrive() {
        progressDialog = new CustomProgressDialog(this);
        progressDialog.show();

        final String inputText = ((EditText)findViewById(R.id.editText)).getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 指定のタイトルのファイルの ID を取得
                    String fileIdOrNull = null;
                    FileList list = service.files().list().execute();
                    for (File f : list.getFiles()) {
                        if (FILE_TITLE.equals(f.getName())) {
                            fileIdOrNull = f.getId();
                        }
                    }

                    File body = new File();
                    body.setName(FILE_TITLE);//fileContent.getName());
                    body.setMimeType("json/application");

                    ByteArrayContent content = new ByteArrayContent("json/application", inputText.getBytes(Charset.forName("UTF-8")));
                    if (fileIdOrNull == null) {
                        service.files().create(body, content).execute();
                        progressDialog.dismiss();
                    } else {
                        service.files().update(fileIdOrNull, body, content).execute();
                        progressDialog.dismiss();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        }).start();
    }

    private void loadTextFromDrive() {
        progressDialog = new CustomProgressDialog(this);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 指定のタイトルのファイルの ID を取得
                    String fileIdOrNull = null;
                    FileList list = service.files().list().execute();
                    for (File f : list.getFiles()) {
                        if (FILE_TITLE.equals(f.getName())) {
                            fileIdOrNull = f.getId();
                        }
                    }

                    InputStream is = null;
                    if (fileIdOrNull != null) {
                        File f = service.files().get(fileIdOrNull).execute();
                        is = downloadFile(service, f);
                    } else{
                        progressDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, getString(R.string.backup_not_found), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    try {
                        StringBuffer sb = new StringBuffer();
                        sb.append(br.readLine());

                        final String text = sb.toString();
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                ((EditText)findViewById(R.id.editText)).setText(text);
                                progressDialog.dismiss();

                                new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.backup_delete_title))
                                    .setMessage(getString(R.string.backup_delete_message))
                                    .setCancelable(false)
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteTextFromDrive();
                                        }
                                    })
                                    .setNegativeButton("NO", null)
                                    .show();
                            }
                        });
                    } finally {
                        if (br != null) br.close();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        }).start();
    }

    private void deleteTextFromDrive() {
        progressDialog = new CustomProgressDialog(this);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 指定のタイトルのファイルを削除
                    FileList list = service.files().list().execute();
                    for (File f : list.getFiles()) {
                        if (FILE_TITLE.equals(f.getName())) {
                            //service.files().trash(f.getId()).execute();
                            service.files().delete(f.getId()).execute();
                        }
                    }

                    progressDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, getString(R.string.backup_delete_complete), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        }).start();
    }

    // https://developers.google.com/drive/v2/reference/files/get より
    private static InputStream downloadFile(Drive service, File file) {
        if (file.getName() != "") {
            try {
                //HttpResponse resp = service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl())).execute();
                //return resp.getContent();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                service.files().get(file.getId()).executeMediaAndDownloadTo(stream);
                return new ByteArrayInputStream(stream.toByteArray());

            } catch (IOException e) {
                // An error occurred.
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
    }

    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
    }

}
