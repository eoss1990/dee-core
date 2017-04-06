package com.seeyon.v3x.dee.util.rest;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub注册表，使用builder避免反射生成Stub，提升性能。
 * 
 * @author wangwenyou
 * 
 */
class StubRegistry {
	interface StubBuilder<T> {
		T build(CTPServiceClientManager cm);
	}

	@SuppressWarnings({ "rawtypes" })
	private static Map<Class, StubBuilder> cache = new HashMap<Class, StubBuilder>();
	private static StubRegistry INSTANCE = new StubRegistry();
	static {
/*		cache.put(AuthorityService.class, new StubBuilder<AuthorityService>() {
			@Override
			public AuthorityService build(CTPServiceClientManager cm) {
				return new AuthorityStub(cm);
			}
		});
		cache.put(OrganizationDataService.class,
				new StubBuilder<OrganizationDataService>() {
					@Override
					public OrganizationDataService build(
							CTPServiceClientManager cm) {
						return new OrganizationDataStub(cm);
					}
				});*/
	}

	private StubRegistry() {

	}

	public static StubRegistry getInstance() {
		return INSTANCE;
	}

	public <T> T newInstance(Class<T> clazz, CTPServiceClientManager cm)
			throws Exception {
		@SuppressWarnings("unchecked")
		StubBuilder<T> builder = cache.get(clazz);
		if (builder == null) {
			// 无Builder，利用命名规则反射生成实例
			String name = clazz.getSimpleName().replace("Service", "");
			Class<?> c = Class.forName("com.seeyon.client.stub." + name
					+ "Stub");
			Constructor constructor = c
					.getDeclaredConstructor(new Class[] { CTPServiceClientManager.class });
			return (T) constructor.newInstance(new Object[] { cm });
		} else {
			return builder.build(cm);
		}
	}
}
