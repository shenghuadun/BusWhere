package com.greenidea.buswhere.ui;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.greenidea.buswhere.ui.SlideToDeleteListView.ItemView.OnItemTouchListener;


/**
 * 可往左侧滑动，显示删除按钮的仿ListView
 * 请通过Helper进行添加、删除等操作
 * @author lenovo
 *
 */
public class SlideToDeleteListView extends ScrollView
{
	private static final Drawable DEL_BTN_PRESSED_COLOR = new ColorDrawable(Color.parseColor("#CC0000"));
	private static final Drawable DEL_BTN_DEFAULT_COLOR = new ColorDrawable(Color.parseColor("#FF4444"));
	
	//分割线
	private static int separatorColor = Color.argb(70, 100, 100, 100);
	
	//选中的颜色
	private static int selectedColor = Color.parseColor("#FFFFCE");
	
	//未选中的颜色
	private static int normalViewColor = Color.parseColor("#F9F9F9");

	private LinearLayout container;
	
	private OnItemEventListener onItemEventListener;
	
	/**
	 * 帮助类，提供列表项增删等方法
	 */
	public Helper helper = new Helper();
	
	private List<ItemView> children = new ArrayList<SlideToDeleteListView.ItemView>();
	
	/*
	 ************************************************************************** 
	 * 								构造方法
	 **************************************************************************
	 */
	public SlideToDeleteListView(Context context)
	{
		super(context);
		
		init(context);
	}
	public SlideToDeleteListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	public SlideToDeleteListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context)
	{
		container = new LinearLayout(context);
		this.addView(container);

		container.setOrientation(LinearLayout.VERTICAL);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		this.setVisibility(View.INVISIBLE);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		this.setVisibility(View.VISIBLE);
	}

	private void refreshIndex()
	{
		int size = container.getChildCount();
		children.clear();
		for(int i=0; i<size; i++)
		{
			View view  = container.getChildAt(i);
			ItemView itemView = (ItemView) view.getTag();
			itemView.index = i;
			
			children.add(itemView);
		}
		
	}
	/**
	 * 添加View作为列表项，可以向左滑动显示删除按钮
	 * @param view
	 */
	private void addDeletableView(View view)
	{
		addDeletableView(view, container.getChildCount());
	}

	/**
	 * 添加View作为列表项，可以向左滑动显示删除按钮
	 * @param view
	 */
	private void addDeletableView(View view, int index)
	{
		ItemView itemView = new ItemView(this.getContext(), true);
		itemView.addContentView(view);
		itemView.setOnItemTouchListener(onItemTouchListener);
		
		container.addView(itemView.container, index);
		children.add(itemView);

		//注册删除按钮的点击事件
		itemView.container.findViewById(ItemView.DEL_BTN_ID).setOnClickListener(onClickListener);
		refreshIndex();
	}
	
	private void addNormalView(View view)
	{
		addNormalView(view, container.getChildCount());
	}
	private void addNormalView(View view, int index)
	{
		ItemView itemView = new ItemView(this.getContext(), false);
		itemView.container.addView(view);
		itemView.container.setBackgroundColor(SlideToDeleteListView.normalViewColor);

		container.addView(itemView.container, index);
		children.add(itemView);
		
		refreshIndex();
	}
	
	/**
	 * 设置列表项，可以向左滑动显示删除按钮
	 * @param list
	 */
	private void setDeletableViews(List<View> viewList)
	{
		container.removeAllViews();
		children.clear();
		
		if(null != viewList && !viewList.isEmpty())
		{
			for(View view : viewList)
			{
				this.addDeletableView(view);
			}
		}
	}

	/**
	 * 添加列表项，可以向左滑动显示删除按钮
	 * @param list
	 */
	private void addDeletableViews(List<View> viewList)
	{
		if(null != viewList && !viewList.isEmpty())
		{
			for(View view : viewList)
			{
				this.addDeletableView(view);
			}
		}
	}
	
	public void removeViewAt(int index)
	{
		container.removeViewAt(index);
		children.remove(index);
		refreshIndex();
	}
	
	private void removeView(ItemView view)
	{
		container.removeView(view.container);
		children.remove(view);
		refreshIndex();
	}

	static int dip2px(float dipValue, Resources resource)
	{
		final float scale = resource.getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	
	public class Helper
	{
		/**
		 * 获取列表项数量
		 * @return
		 */
		public int getChildCount()
		{
			return SlideToDeleteListView.this.container.getChildCount();
		}

		/**
		 * 获取第index个客户添加的view
		 * @param index
		 * @return
		 */
		public View getChildAt(int index)
		{
			return container.getChildAt(index);
		}
		
		public boolean isItemDeletable(int index)
		{
			return children.get(index).isDeletable;
		}
		/**
		 * 添加View作为列表项，可以向左滑动显示删除按钮
		 * @param view
		 */
		public void addDeletableView(View view)
		{
			SlideToDeleteListView.this.addDeletableView(view);
		}

		/**
		 * 添加View作为列表项，可以向左滑动显示删除按钮
		 * @param view
		 */
		public void addDeletableView(View view, int index)
		{
			SlideToDeleteListView.this.addDeletableView(view, index);
		}
		/**
		 * 添加View作为列表项，可以向左滑动显示删除按钮
		 * @param view
		 */
		public void addNormalView(View view)
		{
			SlideToDeleteListView.this.addNormalView(view);
		}

		/**
		 * 添加View作为列表项，可以向左滑动显示删除按钮
		 * @param view
		 */
		public void addNormalView(View view, int index)
		{
			SlideToDeleteListView.this.addNormalView(view, index);
		}
		
		/**
		 * 设置列表项，可以向左滑动显示删除按钮
		 * @param list
		 */
		public void setDeletableViews(List<View> viewList)
		{
			SlideToDeleteListView.this.setDeletableViews(viewList);
		}
		
		/**
		 * 添加一系列View到当前项后面
		 * @param viewList
		 */
		public void addDeletableViews(List<View> viewList)
		{
			SlideToDeleteListView.this.addDeletableViews(viewList);
		}

		/**
		 * 删除列表项
		 * @param view
		 */
		public void removeViewAt(int index)
		{
			SlideToDeleteListView.this.removeViewAt(index);
		}

		/**
		 * 删除列表项
		 * @param view
		 */
		public void removeView(ItemView view)
		{
			SlideToDeleteListView.this.removeView(view);
		}

		/**
		 * 用于监听删除按钮点击事件、列表项选择事件
		 * @param listener
		 */
		public void setOnItemEventListener(OnItemEventListener listener)
		{
			SlideToDeleteListView.this.onItemEventListener = listener;
		}
		
		/**
		 * 删除所有内容
		 */
		public void removeAllViews()
		{
			SlideToDeleteListView.this.container.removeAllViews();
			SlideToDeleteListView.this.children.clear();
		}
		
		public void setPadding(int left, int top, int right, int bottom)
		{
			SlideToDeleteListView.this.container.setPadding(left, top, right, bottom);
		}
		public void setPadding(int padding)
		{
			SlideToDeleteListView.this.container.setPadding(padding, padding, padding, padding);
		}
		
		/**
		 * 将黄色按下效果恢复成白色，并隐藏删除按钮
		 */
		public void resetPressState()
		{
			int count = container.getChildCount();
			
			for(int i=0; i<count; i++)
			{
				View view = container.getChildAt(i);
				ItemView itemView = (ItemView) view.getTag();
				if(itemView.isDeletable)
				{
					itemView.hideDelBtn();
					itemView.content.setBackgroundColor(Color.WHITE);
				}
			}
		}
	}
	
	/**
	 * 当删除按钮点击、当前项被选中时，应用程序可通过本接口处理相应事件
	 * @author lenovo
	 *
	 */
	public static interface OnItemEventListener
	{
		/**
		 * 点击当前项的删除按钮，将在项从组件中删除之前调用
		 * @param item
		 */
		void onItemDelete(SlideToDeleteListView slideToDeleteListView, int index, View item);
		
		/**
		 * 当前项被选中
		 * @param item
		 */
		void onItemSelected(SlideToDeleteListView slideToDeleteListView, int index, View item);
	}
	
	private View.OnClickListener onClickListener = new OnClickListener()
	{
		@Override
		public void onClick(final View v)
		{
			//点击的是删除按钮
			if(null != onItemEventListener)
			{
				if(ItemView.DEL_BTN_ID == v.getId())
				{
					AnimationSet animationSet = new AnimationSet(false);
					
					AlphaAnimation animation = new AlphaAnimation(1, 0);
					
					animationSet.addAnimation(animation);
					
					TranslateAnimation translateAnimation = new TranslateAnimation(0, 0 - ((ItemView) v.getTag()).container.getWidth(), 0, 0);
					
					animationSet.addAnimation(translateAnimation);
					animationSet.setDuration(300);
					animationSet.setFillAfter(true);

					animationSet.setAnimationListener(new AnimationListener()
					{
						@Override
						public void onAnimationStart(Animation animation)
						{
						}
						
						@Override
						public void onAnimationRepeat(Animation animation)
						{
						}
						
						@Override
						public void onAnimationEnd(Animation animation)
						{
							SlideToDeleteListView.this.postDelayed(new Runnable()
							{
								@Override
								public void run()
								{
									onItemEventListener.onItemDelete(SlideToDeleteListView.this, ((ItemView) v.getTag()).index, ((ItemView) v.getTag()).content.getChildAt(0));	
									SlideToDeleteListView.this.helper.removeView(((ItemView) v.getTag()));	
								}
							}, 20);		
							
						}
					});
					
					((ItemView) v.getTag()).container.startAnimation(animationSet);
					
				}
			}
			
		}
	};
	
	//拦截ItemView的触摸事件，以便将其他已显示删除按钮的项复位
	private ItemView.OnItemTouchListener onItemTouchListener = new ItemView.OnItemTouchListener()
	{
		@Override
		public void onItemTouch(final ItemView itemView, boolean isSelect)
		{
			if(isSelect)
			{
				if(null != onItemEventListener)
				{
					onItemEventListener.onItemSelected(SlideToDeleteListView.this, itemView.index, itemView.content.getChildAt(0));
				}
				itemView.container.postDelayed(new Runnable()
				{
					
					@Override
					public void run()
					{
						itemView.hideDelBtn();
					}
				}, 200);
				itemView.content.setBackgroundColor(SlideToDeleteListView.selectedColor);
			}
			
			int count = SlideToDeleteListView.this.container.getChildCount();
			
			for(int i=0; i<count; i++)
			{
				View view = SlideToDeleteListView.this.container.getChildAt(i);
				Object tag = view.getTag();
				if(tag != null && tag instanceof ItemView && tag != itemView && ((ItemView)tag).isDeletable)
				{
					((ItemView) view.getTag()).hideDelBtn();

					((ItemView) view.getTag()).content.setBackgroundColor(Color.WHITE);
				}
			}
		}
	};
	

	static class ItemView
	{
		private static final int DEL_WIDTH = 70;
		
		private static final int MIN_HEIGHT = 48;
		
		//删除按钮ID
		private static final int DEL_BTN_ID = 11001; 
		//占位区ID
		private static final int PLACE_HOLDER_ID = 11003; 
		//内容区ID
		private static final int CONTENT_ID = 11002; 
		
		private Button delBtn;

		/**
		 * 在列表中的索引
		 */
		public int index;
		//从上往下嵌套
		private RelativeLayout container;
		private HOverScrollView scroller;
		private LinearLayout placeholder;
		private RelativeLayout content;
		
		private boolean isFinishedLayout = false;
		public boolean isDeletable;
		
		public ItemView(Context context, boolean isDeletable)
		{
			this.index = index;
			container = new RelativeLayout(context);
			this.isDeletable = isDeletable;
			
			//用于调用hideDelBtn()方法及设置index
			container.setTag(this);
			if(isDeletable)
			{
				container.setBackgroundColor(SlideToDeleteListView.separatorColor);
				container.setPadding(0, 1, 0, 0);
				
				//1.生成删除按钮
				delBtn = new Button(context, null, android.R.attr.buttonStyleSmall);
				delBtn.setId(ItemView.DEL_BTN_ID);
				delBtn.setTextColor(Color.WHITE);
	
				//设置按钮默认、按下背景色
				StateListDrawable drawable = new StateListDrawable();
				drawable.addState(new int[]{android.R.attr.state_pressed}, SlideToDeleteListView.DEL_BTN_PRESSED_COLOR);
				drawable.addState(new int[]{}, SlideToDeleteListView.DEL_BTN_DEFAULT_COLOR);
				delBtn.setBackgroundDrawable(drawable);
				delBtn.setText("删除");
				
				//靠右对齐
				RelativeLayout.LayoutParams del_param = new RelativeLayout.LayoutParams(SlideToDeleteListView.dip2px(ItemView.DEL_WIDTH, delBtn.getResources()), RelativeLayout.LayoutParams.MATCH_PARENT);
				del_param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				del_param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				
				delBtn.setLayoutParams(del_param);
				delBtn.setTag(this);
				
				//因为container背景色为灰色，删除按钮淡入淡出效果需要白色背景，所以此处添加一个白色的图片
				ImageView whiteBack = new ImageView(context);
				whiteBack.setBackgroundColor(Color.WHITE);
				whiteBack.setLayoutParams(del_param);
				container.addView(whiteBack);
				container.addView(delBtn);
	
				//2.为了能够撑开HOverScrollView，需要生成一个占位视图
				placeholder = new LinearLayout(context);
				placeholder.setId(PLACE_HOLDER_ID);
				
				//3.放置内容的视图，宽高是按内容适配的
				content = new RelativeLayout(context);
				content.setId(ItemView.CONTENT_ID);
				content.setBackgroundColor(Color.WHITE);
				
				placeholder.addView(content);
				
				
				//4.水平滚动视图
				scroller = new HOverScrollView(context, placeholder, this);
				scroller.setHorizontalFadingEdgeEnabled(false);
				container.addView(scroller);
	
				//5.在显示前，设置内容的宽度和占位视图的padding
				setOnPreDrawListener();
			}
		}

		private void setOnPreDrawListener()
		{
			placeholder.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener()
			{
				@Override
				public boolean onPreDraw()
				{
					if(!isFinishedLayout)
					{
						placeholder.setPadding(0, 0, SlideToDeleteListView.dip2px(DEL_WIDTH, placeholder.getResources()) + HOverScrollView.overScrollDistance_PX, 0);

						int height = placeholder.getMeasuredHeight();
						
						int minHeight = SlideToDeleteListView.dip2px(MIN_HEIGHT, content.getResources());
						if(height < minHeight)
						{
							height = minHeight;
						}
					}
					
					return true;
				}
			});
			
			delBtn.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener()
			{
				@Override
				public boolean onPreDraw()
				{
					if(!isFinishedLayout)
					{

						int height = placeholder.getMeasuredHeight();
						
						int minHeight = SlideToDeleteListView.dip2px(MIN_HEIGHT, content.getResources());
						if(height < minHeight)
						{
							height = minHeight;
						}
						delBtn.setHeight(height);
						delBtn.invalidate();
					}
					
					return true;
				}
			});

			content.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener()
			{
				@Override
				public boolean onPreDraw()
				{
					if(!isFinishedLayout)
					{
						int width = container.getMeasuredWidth();
						
						//屏幕关闭时，宽度值可能为0
						if(0 != width)
						{
							//最后执行onPreDraw的view
							isFinishedLayout = true;
						}
						
						width = (0 == width) ? 640 : width;
						
						int height = placeholder.getMeasuredHeight();
						
						int minHeight = SlideToDeleteListView.dip2px(MIN_HEIGHT, content.getResources());
						if(height < minHeight)
						{
							height = minHeight;
						}
						Log.d("高度", height +"");
						
						LinearLayout.LayoutParams content_param = 
								new LinearLayout.LayoutParams(width, height);
						content.setLayoutParams(content_param);
						content.postInvalidate();
					}
					
					return true;
				}
			});
		}
		
		void setOnItemTouchListener(OnItemTouchListener onItemTouchListener)
		{
			if(isDeletable)
			{
				this.scroller.setOnItemTouchListener(onItemTouchListener);
			}
		}
		
		void hideDelBtn()
		{
			if(isDeletable && this.scroller.getScrollX() != 0)
			{
				this.scroller.bringToFront();
				this.scroller.smoothScrollTo(0, 0);
			}
		}
		void showDelBtn()
		{
			if(isDeletable && this.scroller.getScrollX() != this.scroller.delBtnWidth_PX)
			{
				this.scroller.bringToFront();
				this.scroller.smoothScrollTo(this.scroller.delBtnWidth_PX, 0);
			}
		}
		
		/**
		 * 通过本方法添加视图，才能滑动删除
		 * @param view
		 */
		void addContentView(View view)
		{
			android.widget.RelativeLayout.LayoutParams p = new android.widget.RelativeLayout.LayoutParams(android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT, android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
			p.addRule(RelativeLayout.CENTER_VERTICAL);
			view.setLayoutParams(p);
			
			this.content.addView(view);
		}

		/**
		 * 用于检测down、up事件，以及是否是选中动作
		 * @author lenovo
		 *
		 */
		public static interface OnItemTouchListener
		{
			void onItemTouch(ItemView itemView, boolean isSelect);
		}
	}
	
	
	private static class HOverScrollView extends HorizontalScrollView
	{
		private static final double FACTOR = 0.3;
		private static final float INVALID_POS = 9999.0f;
		private View contentView;
		private View delBtn;
		private ItemView parent;
		
		//在action_up之前，是否scroll了
		private boolean isScrolling;
		
		private OnItemTouchListener onItemTouchListener;
		
		//删除按钮的宽度
		private int delBtnWidth_PX = SlideToDeleteListView.dip2px(ItemView.DEL_WIDTH, getResources());
		
		//拖拽起点
		private float down_x = INVALID_POS;
		//上个移动的点
		private float last_event_x = INVALID_POS;
		//视图最大滚动距离
		private float scroll_x = INVALID_POS;
		
		private boolean moveToRight = false;

		//因为往左移动会变慢，右移又会变为常速，每次来回滑动会积累下偏移量
		private float offset = 0;
		
		public static final int overScrollDistance_PX = 500;
		
		public HOverScrollView(Context context, View content, ItemView parent)
		{
			super(context);
//			this.setOnTouchListener(listener);
			
			this.contentView = content;
			this.parent = parent;
			this.delBtn = parent.delBtn;
			this.setHorizontalScrollBarEnabled(false);

			this.addView(contentView);
		}
	    
		public void setOnItemTouchListener(OnItemTouchListener onItemTouchListener)
		{
			this.onItemTouchListener = onItemTouchListener;
		}
		
		@Override
		public boolean dispatchTouchEvent(MotionEvent ev)
		{
			switch (ev.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				onItemTouchListener.onItemTouch(parent, false);
				
				down_x = ev.getX();
				last_event_x = down_x;
				scroll_x = down_x;
				
				offset = 0;
				
				this.bringToFront();
				break;
				
			case MotionEvent.ACTION_MOVE:
				//普通点击，也有可能触发move，所以加上阈值
				if(!isScrolling && getScrollX() > 20)
				{
					isScrolling = true;
				}
				
				float temp = last_event_x;
				last_event_x = ev.getX();
				
				//往左移动
				if(ev.getX() < temp)
				{
					moveToRight = false;

					//未超出删除按钮宽度
					if(getScrollX() < HOverScrollView.this.delBtnWidth_PX )
					{
						down_x = ev.getX() + offset;
						ev.setLocation(down_x, ev.getY());
						Log.d("", "未超出删除按钮宽度");
					}
					//已超出删除按钮宽度
					else
					{
						scroll_x = (float) ((ev.getX() + offset - down_x) * FACTOR + down_x);
					
						ev.setLocation(scroll_x, ev.getY());
					}
				}
				//往右移动
				else
				{
					if(!moveToRight)
					{
						moveToRight = true;

						offset = scroll_x - ev.getX();
						Log.d("offset", offset+"--");
					}
					ev.setLocation(ev.getX() + offset, ev.getY());
					
					//已经移动到最右侧
					if(this.getScrollX() == 0)
					{
						return true;
					}
				}

				break;
			case MotionEvent.ACTION_UP:
				Rect rect = new Rect();
				this.getHitRect(rect);
				
				//抬起点还在视图上
				boolean isSelect =rect.contains((int)ev.getX(), (int)ev.getY());
				//没有显示删除按钮
				isSelect = isSelect && this.getScrollX() == 0;
				//未向右滑动
				isSelect = isSelect && down_x - ev.getX() > -5;
				//未长距离滑动
				isSelect = isSelect && !isScrolling;
				
				//删除按钮完全露出来了
				isSelect = isSelect || this.getScrollX() == HOverScrollView.this.delBtnWidth_PX;
				
				onItemTouchListener.onItemTouch(parent, isSelect);
				
				isScrolling = false;
				
				

				Message msg = animationHandler.obtainMessage();
				msg.obj = HOverScrollView.this;
				if(HOverScrollView.this.getScrollX() > HOverScrollView.this.delBtnWidth_PX / 2)
				{
					msg.what = HOverScrollView.this.delBtnWidth_PX;
				}
				else 
				{
					msg.what = 0;
				}
				animationHandler.sendMessage(msg);
				
				break;
				
			default:
				break;
			}
			
			return super.dispatchTouchEvent(ev);
		}

		private static Handler animationHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				
				((HOverScrollView)msg.obj).smoothScrollTo(msg.what, 0);
				
				if(0 != msg.what)
				{
					((HOverScrollView)msg.obj).delBtn.bringToFront();
				}
				else 
				{
					((HOverScrollView)msg.obj).bringToFront();
				}
			}
		};

	}
	

	public static class LayoutParams extends LinearLayout.LayoutParams
	{

		public LayoutParams(int width, int height)
		{
			super(width, height);
		}
	}
	
}
