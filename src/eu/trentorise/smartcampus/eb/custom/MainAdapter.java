/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.eb.custom;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.fragments.CollectionsListFragment;
import eu.trentorise.smartcampus.eb.fragments.ExperiencesListFragment;
import eu.trentorise.smartcampus.eb.fragments.GrabDialogFragment;
import eu.trentorise.smartcampus.eb.fragments.SearchFragment;

public class MainAdapter extends BaseAdapter {
	private Context context;
	private FragmentManager fragmentManager;
	
	private int currentlyOpened = -1;

	public MainAdapter(Context c) {
		this.context = c;
	}

	public MainAdapter(Context applicationContext, FragmentManager fragmentManager) {
		this.fragmentManager = fragmentManager;
		this.context = applicationContext;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
		if (convertView == null) {
			holder.text = new TextView(context);
			holder.text.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
			holder.text.setCompoundDrawablesWithIntrinsicBounds(null,
					context.getResources().getDrawable(MAIN_ACTIONS[position].thumbnail), null, null);
			holder.text.setText(MAIN_ACTIONS[position].description);
			holder.text.setTextColor(context.getResources().getColor(R.color.sc_dark_gray));
			holder.text.setGravity(Gravity.CENTER);

			holder.text.setOnClickListener(new ExperienceOnClickListener(position));

		} else {
			holder.text = (TextView) convertView;
			holder.text.setText(((TextView) convertView).getText());

			holder.text.setBackgroundColor(context.getResources().getColor(
					android.R.color.transparent));
			holder.text.setCompoundDrawablesWithIntrinsicBounds(null, context
					.getResources().getDrawable(MAIN_ACTIONS[position].thumbnail), null, null);
			holder.text.setTextColor(((TextView) convertView).getTextColors());
			holder.text.setGravity(((TextView) convertView).getGravity());
			holder.text.setOnClickListener(new ExperienceOnClickListener(position));
		}

		return holder.text;
	}

	static class ViewHolder{
		TextView text;
	}
	
	public class ExperienceOnClickListener implements OnClickListener {
		int position;
		
		public ExperienceOnClickListener(int position) {
			this.position=position;
	}

		@Override
		public void onClick(View v) {
			open(position);
		}
		
	}
	
	
	private void open(final int position) {
		currentlyOpened = position;
		if (MAIN_ACTIONS[position].fragmentClass != null) {
			// Starting transaction
			FragmentTransaction ft = fragmentManager.beginTransaction();
			Fragment fragment = (Fragment) Fragment.instantiate(context,
					MAIN_ACTIONS[position].fragmentClass.getName());
			if (fragment instanceof DialogFragment) {
				((DialogFragment) fragment).show(ft, "dialog");
			} else {
				// Replacing old fragment with new one
				ft.replace(android.R.id.content, fragment);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.addToBackStack(null);
				ft.commit();
			}
		} else {
			Toast toast = Toast.makeText(context, "TODO", Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	public int getCurrentlyOpened() {
		return currentlyOpened;
	}

	public void setCurrentlyOpened(int currentlyOpened) {
		open(currentlyOpened);
	}

	@Override
	public int getCount() {
		return MAIN_ACTIONS.length;
	}

	@Override
	public Object getItem(int arg0) {
		return MAIN_ACTIONS[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	public static class MainActionDescriptor {
		public int description;
		public int thumbnail;
		public Class<? extends Fragment> fragmentClass;

		public MainActionDescriptor(int description, int thumbnail, Class<? extends Fragment> fragmentClass) {
			super();
			this.description = description;
			this.thumbnail = thumbnail;
			this.fragmentClass = fragmentClass;
		}
	}

	private static MainActionDescriptor[] MAIN_ACTIONS = new MainActionDescriptor[] {
			// new MainActionDescriptor(R.string.mainmenu_search,
			// R.drawable.ic_search, SearchFragment.class),
			new MainActionDescriptor(R.string.mainmenu_grab, R.drawable.ic_grab, GrabDialogFragment.class),
			new MainActionDescriptor(R.string.mainmenu_diary, R.drawable.ic_diary, ExperiencesListFragment.class),
			new MainActionDescriptor(R.string.mainmenu_collections, R.drawable.ic_collections,
					CollectionsListFragment.class),
			new MainActionDescriptor(R.string.mainmenu_search, R.drawable.ic_search, SearchFragment.class) };
}
