const jwtDecode = jest.fn().mockImplementation((_token) => ({
  sub: 'test@example.com',
  exp: Math.floor(Date.now() / 1000) + 3600, // 1 hour from now
  role: 'TEACHER',
  userId: 1,
  firstName: 'Test',
  lastName: 'User',
  email: 'test@example.com'
}));

export default jwtDecode;
